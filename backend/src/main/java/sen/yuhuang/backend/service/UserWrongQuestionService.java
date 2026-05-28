package sen.yuhuang.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.dto.TestSampleResultsDto;
import sen.yuhuang.backend.dto.UserWrongQuestionDTO;
import sen.yuhuang.backend.entity.*;
import sen.yuhuang.backend.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserWrongQuestionService {
    @Autowired
    private UserWrongQuestionRepository userWrongQuestionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionBankRepository questionBankRepository;

    @Autowired
    private SampleQuestionRepository sampleQuestionRepository;

    @Autowired
    private TestSampleResultRepository testSampleResultRepository;

    @Autowired
    private TestSampleRepository testSampleRepository;


    public Result getWrongBankCount(String userId) {
        try {
            Long userIdLong = Long.parseLong(userId);
            List<Object[]> results = userWrongQuestionRepository.countByUserIdGroupByBank(userIdLong);
            Map<Long, Integer> wrongMap = new HashMap<>();
            for (Object[] result : results) {
                Long bankId = (Long) result[0];
                Integer count = ((Number) result[1]).intValue();
                wrongMap.put(bankId, count);
            }
            return Result.ok(wrongMap);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取错题数量失败：" + e.getMessage());
        }
    }

    public Result getWrongQuestionList(String userId) {
        try{
            Long userIdLong = Long.parseLong(userId);

            ArrayList<UserWrongQuestionDTO> arrayList = new ArrayList<>();

            // 1.获取所有该用户的错题
            List<UserWrongQuestion> wrongQuestions = userWrongQuestionRepository.findUserWrongQuestionByUserId(userIdLong);

            // 2.封装数据
            for (UserWrongQuestion wrongQuestion : wrongQuestions) {
                // 1.查询错题所属题库
                Optional<QuestionBank> bank = questionBankRepository.findById(wrongQuestion.getQuestionBankId());
                if (!bank.isPresent()) return Result.error("错题的题库不存在!");

                // 2.查询错题的原题
                Optional<Question> question = questionRepository.findById(wrongQuestion.getQuestionId());
                if (!question.isPresent()) return Result.error("错题的原题不存在！");

                // 3.查询题的样例
                List<SampleQuestion> sample = sampleQuestionRepository.
                        findSampleQuestionByQuestionId(wrongQuestion.getQuestionId());

                // 4.查询测试用例结果
                List<TestSampleResult> testSample = testSampleResultRepository.
                        findTestSampleResultByUserIdAndQuestionId(userIdLong,wrongQuestion.getQuestionId());

                ArrayList<TestSampleResultsDto> testSampleResultsDtos = new ArrayList<>();
                // 5.封装测试用例结果+测试用例
                for (TestSampleResult result :testSample){
                    TestSampleResultsDto dto = new TestSampleResultsDto();

                    TestSample testSampleById = testSampleRepository.findTestSampleById(result.getTestSampleId());

                    dto.setName(testSampleById.getId().toString());
                    dto.setIsPassed(String.valueOf(result.getIsPassed()));
                    dto.setUsedTime(String.valueOf(result.getUsedTime()));
                    dto.setErrorMessage(result.getErrorMsg());
                    dto.setInput(testSampleById.getInput());
                    dto.setOutput(testSampleById.getOutput());
                    dto.setActualOutput(result.getActualOutput());
                    testSampleResultsDtos.add(dto);
                }

                // 3.封装DTO
                UserWrongQuestionDTO dto = new UserWrongQuestionDTO();
                dto.setId(wrongQuestion.getId());
                dto.setQuestionId(wrongQuestion.getQuestionId());
                dto.setQuestionBankId(wrongQuestion.getQuestionBankId());
                dto.setBankTitle(bank.get().getTitle());
                dto.setQuestionDesc(question.get().getQuestionDesc());
                dto.setLevel(bank.get().getLevel().toString());
                dto.setInputFormat(question.get().getInputFormat());
                dto.setOutputFormat(question.get().getOutputFormat());
                dto.setHint(question.get().getHint());
                dto.setUserAnswer(wrongQuestion.getUserAnswer());
                dto.setCorrectAnswer(wrongQuestion.getCorrectAnswer());
                dto.setErrorMessage(wrongQuestion.getErrorInfo());
                dto.setLastWrongTime(wrongQuestion.getLastWrongTime());
                dto.setCode(wrongQuestion.getCode());
                dto.setSampleQuestionList(sample);
                dto.setTestSampleResultList(testSampleResultsDtos);
                arrayList.add(dto);
            }
            return Result.ok(arrayList);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    public Result removeWrongQuestion(String userId,String wrongId) {
        try {
            Long wrongIdLong = Long.parseLong(wrongId);
            Long userIdLong =  Long.parseLong(userId);

            UserWrongQuestion wrongQuestion = userWrongQuestionRepository.findUserWrongQuestionsByUserIdAndQuestionId(userIdLong, wrongIdLong);
            if (wrongQuestion == null)
                return Result.error("不存在该错题！");
            userWrongQuestionRepository.deleteById(wrongQuestion.getId());
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
}
