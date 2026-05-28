package sen.yuhuang.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.dto.QuestionBankDTO;
import sen.yuhuang.backend.dto.QuestionListDTO;
import sen.yuhuang.backend.entity.*;
import sen.yuhuang.backend.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionBankService {

    @Autowired
    private QuestionBankRepository questionBankRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCollectBankRepository userCollectBankRepository;

    @Autowired
    private UserWrongQuestionRepository userWrongQuestionRepository;

    @Autowired
    private TestSampleResultRepository testSampleResultRepository;

    /**
     * 获取题库列表
     */
    public Result getBankList(Integer page, Integer size, String keyword, String tab, Long userId) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size);

            Map<String, Object> result = new HashMap<>();
            result.put("page", page);
            result.put("size", size);

            Page<QuestionBankDTO> dtoPage = null;

            if ("collected".equals(tab)) {
                // 查询收藏的题库
                Page<UserCollectBank> collectPage = userCollectBankRepository.findCollectedBanks(userId, keyword, pageable);

                List<Long> bankIds = collectPage.getContent().stream()
                        .map(UserCollectBank::getQuestionBankId)
                        .collect(Collectors.toList());

                List<QuestionBank> banks = questionBankRepository.findAllById(bankIds);

                // 转换为 DTO
                List<QuestionBankDTO> dtoList = banks.stream()
                        .map(bank -> convertToDTO(bank, true, 0))
                        .collect(Collectors.toList());

                dtoPage = new PageImpl<>(dtoList, pageable, collectPage.getTotalElements());

            } else if ("wrong".equals(tab)) {
                // 查询错题本
                Page<UserWrongQuestion> wrongPage = userWrongQuestionRepository.findWrongQuestionBanks(userId, keyword, pageable);

                List<Long> bankIds = wrongPage.getContent().stream()
                        .map(UserWrongQuestion::getQuestionBankId)
                        .collect(Collectors.toList());

                List<QuestionBank> banks = questionBankRepository.findAllById(bankIds);

                // 获取每个题库的错题数量
                Map<Long, Integer> wrongCountMap = new HashMap<>();
                for (Long bankId : bankIds) {
                    Integer count = userWrongQuestionRepository.countByUserIdAndBankId(userId, bankId);
                    wrongCountMap.put(bankId, count);
                }

                // 转换为 DTO
                List<QuestionBankDTO> dtoList = banks.stream()
                        .map(bank -> convertToDTO(bank, false, wrongCountMap.getOrDefault(bank.getId(), 0)))
                        .collect(Collectors.toList());

                dtoPage = new PageImpl<>(dtoList, pageable, wrongPage.getTotalElements());

            } else {
                // 查询全部题库
                User user = userRepository.findUserByUserId(userId);
                if (user == null) return Result.error("获取题库，用户不存在！");

                Integer vip = null;
                LocalDateTime vipExpireTime = user.getVipExpireTime();
                if (vipExpireTime == null || vipExpireTime.isBefore(LocalDateTime.now())) {
                    vip = 0; // 非VIP用户只能看普通题库
                }

                Page<QuestionBank> bankPage = questionBankRepository.findAllBanks(keyword, pageable, vip);

                // 获取用户收藏的题库ID列表
                List<Long> collectedBankIds = userCollectBankRepository.findBankIdsByUserId(userId);

                // 转换为 DTO
                List<QuestionBankDTO> dtoList = bankPage.getContent().stream()
                        .map(bank -> {
                            boolean collected = collectedBankIds.contains(bank.getId());
                            return convertToDTO(bank, collected, 0);
                        })
                        .collect(Collectors.toList());

                dtoPage = new PageImpl<>(dtoList, pageable, bankPage.getTotalElements());
            }

            result.put("data", dtoPage);
            result.put("total", dtoPage.getTotalElements());

            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取题库列表失败：" + e.getMessage());
        }
    }

    /**
     * 转换实体为 DTO
     */
    private QuestionBankDTO convertToDTO(QuestionBank bank, boolean collected, int wrongCount) {
        QuestionBankDTO dto = new QuestionBankDTO();
        dto.setId(bank.getId());
        dto.setTitle(bank.getTitle());
        dto.setDesc(bank.getDesc());

        // 转换难度等级
        String levelName = "未知";
        if (bank.getLevel() != null) {
            switch (bank.getLevel()) {
                case 1: levelName = "入门"; break;
                case 2: levelName = "简单"; break;
                case 3: levelName = "中等"; break;
                case 4: levelName = "困难"; break;
            }
        }
        dto.setLevel(levelName);

        dto.setQuestionCount(bank.getQuestionCount());
        dto.setRecommend(bank.getRecommend());
        dto.setViewCount(bank.getViewCount());
        dto.setCollected(collected);
        dto.setWrongCount(wrongCount);
        dto.setIsVip(bank.getIsVip());

        return dto;
    }

    public Result getAllQuestionList(String userId, String bankId) {
        try {
            Long userIdLong = Long.parseLong(userId);
            Long bankIdLong = Long.parseLong(bankId);


            // 1.查询用户是否具有这个题目的权限
            User user = userRepository.findUserByUserId(userIdLong);
            if (user == null) return Result.error("获取题目列表时候，用户不存在！");

            // 2.查询题库
            Optional<QuestionBank> questionBank = questionBankRepository.findById(bankIdLong);
            if (!questionBank.isPresent()) return Result.error("获取题目列表时候，题库不存在！");

            // 判断用户是否是VIP
            LocalDateTime vipExpireTime = user.getVipExpireTime();

            // 判断题库是否是VIP题库
            if ((questionBank.get().getIsVip() == 1) && (vipExpireTime == null || vipExpireTime.isBefore(LocalDateTime.now()))){
                return Result.ok(null).setMessage("用户会员到期！");
            }
            // 3.查询题目
            List<Question> question = questionRepository.findQuestionByBankId(bankIdLong);
            ArrayList<QuestionListDTO> listDTOS = new ArrayList<>();
            for (Question q : question) {
                int successCount = 0;
                int errorCount = 0;

                QuestionListDTO dto = new QuestionListDTO();
                dto.setQuestionId(q.getId());
                dto.setQuestionDesc(q.getQuestionDesc());
                dto.setLevel(q.getLevel());
                dto.setParticipantCount(q.getParticipantCount());
                // 1.查询测试用例记录
                List<TestSampleResult> testSample = testSampleResultRepository
                        .findTestSampleResultByUserIdAndQuestionId(userIdLong, q.getId());
                for (TestSampleResult testSampleResult : testSample) {
                    if(testSampleResult.getIsPassed() == 1){
                        successCount++;
                    }else {
                        errorCount++;
                    }
                }
                if ((successCount + errorCount) == 0){
                    dto.setAC(BigDecimal.valueOf(0));
                }else {
                    BigDecimal acRate = new BigDecimal(successCount)
                            .divide(new BigDecimal(successCount+errorCount), 2, RoundingMode.HALF_UP); // 四舍五入保留两位
                    dto.setAC(acRate);
                }
                //2.查询错误记录
                UserWrongQuestion record = userWrongQuestionRepository.
                        findUserWrongQuestionsByUserIdAndQuestionId(userIdLong, q.getId());
                if (record != null) {
                    dto.setWrong(true);
                }
                listDTOS.add(dto);
            }
            return Result.ok(listDTOS);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    public Result addCollectBank(String userId, String bankId, Boolean collected) {
        try {
            Long userIdLong = Long.parseLong(userId);
            Long  bankIdLong = Long.parseLong(bankId);


            List<QuestionBank> questionBankById = questionBankRepository.findQuestionBankById(bankIdLong);

            if (questionBankById == null)
                return Result.error("收藏的题库不存在!");

            UserCollectBank newCollected = new UserCollectBank();
            newCollected.setUserId(userIdLong);
            newCollected.setQuestionBankId(bankIdLong);

            if (collected) {
                // 收藏的逻辑
                userCollectBankRepository.save(newCollected);
            }else {
                // 取消收藏
                userCollectBankRepository.deleteUserCollectBankByUserIdAndQuestionBankId(userIdLong, bankIdLong);
            }

            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    public Result addViewCount(String bankId) {
        try {
            // 1.查询该题库
            Optional<QuestionBank> bank = questionBankRepository.findById(Long.valueOf(bankId));
            if (bank.isPresent()) {
                // 更新
                questionBankRepository.updateViewCountById(bank.get().getId(),bank.get().getViewCount() + 1);
                return Result.ok();
            }else {
                return Result.error("题库不存在！");
            }
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    public Result loadBankList() {
        try {
            // 1.查询该题库
            List<QuestionBank> bankList = questionBankRepository.findAll();
            return Result.ok(bankList);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    public Result saveBank(Map<String, Object> request) {
        try {
            String bankIdText = toText(request.get("id"));
            String title = toText(request.get("title"));
            String desc = toText(request.get("desc"));
            String difficultyText = toText(request.get("difficulty"));
            String needVipText = toText(request.get("needVip"));
            String recommendText = toText(request.get("recommend"));
            String statusText = toText(request.get("status"));

            if (title == null || title.isEmpty()) {
                return Result.error("题库名称不能为空");
            }

            QuestionBank bank;
            if (bankIdText != null && !bankIdText.isEmpty()) {
                Long bankId = Long.valueOf(bankIdText);
                bank = questionBankRepository.findById(bankId).orElse(null);
                if (bank == null) {
                    return Result.error("题库不存在");
                }
            } else {
                bank = new QuestionBank();
                bank.setQuestionCount(0);
                bank.setViewCount(0);
            }

            bank.setTitle(title);
            bank.setDesc(desc == null ? "" : desc);
            bank.setLevel(difficultyText == null || difficultyText.isEmpty() ? 1 : Integer.parseInt(difficultyText));
            bank.setIsVip(needVipText == null || needVipText.isEmpty() ? 0 : Integer.parseInt(needVipText));
            bank.setRecommend(recommendText == null || recommendText.isEmpty() ? 80 : Integer.parseInt(recommendText));
            bank.setStatus(statusText == null || statusText.isEmpty() ? 1 : Integer.parseInt(statusText));

            QuestionBank saved = questionBankRepository.save(bank);
            return Result.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("保存题库失败：" + e.getMessage());
        }
    }

    private String toText(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text) || "undefined".equalsIgnoreCase(text)) {
            return null;
        }
        return text;
    }

    @Transactional
    public Result deleteBank(String bankId) {
        try {
            Long bankIdLong = Long.valueOf(bankId);
            Optional<QuestionBank> bank = questionBankRepository.findById(bankIdLong);
            if (bank.isEmpty()) {
                return Result.error("题库不存在");
            }

            List<Question> questions = questionRepository.findQuestionByBankId(bankIdLong);
            if (!questions.isEmpty()) {
                return Result.error("该题库下存在题目，请先删除相关题目");
            }

            questionBankRepository.deleteById(bankIdLong);
            return Result.ok().setMessage("删除题库成功");
        } catch (NumberFormatException e) {
            return Result.error("题库ID格式错误");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除题库失败：" + e.getMessage());
        }
    }
}
