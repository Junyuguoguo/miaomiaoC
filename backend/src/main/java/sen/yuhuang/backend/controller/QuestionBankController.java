package sen.yuhuang.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.service.QuestionBankService;
import sen.yuhuang.backend.service.QuestionService;
import sen.yuhuang.backend.service.UserCollectBankService;
import sen.yuhuang.backend.service.UserWrongQuestionService;

import java.util.Map;

@RestController
@RequestMapping("/api/bank/")
public class QuestionBankController {

    @Autowired
    private QuestionBankService questionBankService;

    @Autowired
    private UserCollectBankService userCollectBankService;

    @Autowired
    private UserWrongQuestionService userWrongQuestionService;

    @Autowired
    private QuestionService questionService;

    @PostMapping("/getBankList")
    public Result getBankList(@RequestBody Map<String,Object> request){
        Integer page = (Integer)request.get("page");
        Integer size = (Integer)request.get("size");
        String keyWord = (String)request.get("keyword");
        String tab = (String)request.get("tab");
        Long userId = Long.valueOf((String)request.get("userId"));

        if (userId == null) return Result.error("获取题库列表，用户ID不可为空！");
        if (size == null) size = 8;
        if (page == null) page = 1;
        if (keyWord == null) keyWord = "";

        return questionBankService.getBankList(page,size,keyWord,tab,userId);
    }
    // 1. 获取用户收藏的题库ID列表
    @PostMapping("/getCollectBankIds")
    public Result getCollectBankIds(@RequestBody Map<String,Object> request) {
        String userId = (String)request.get("userId");
        if (userId == null) return Result.error("获取用户收藏题库ID列表,用户空！");
        return userCollectBankService.getCollectBankIds(userId);
    }

    // 2. 获取用户每个题库的错题数量
    @PostMapping("/getWrongBankCount")
    public Result getWrongBankCount(@RequestBody Map<String,Object> request) {
        String userId = (String) request.get("userId");
        if (userId == null) return Result.error("获取用户每个题库的错题数量,用户空！");
        return userWrongQuestionService.getWrongBankCount(userId);
    }

    @PostMapping("/getAllQuestionList")
    public Result getAllQuestionList(@RequestBody Map<String,Object> request) {
        String userId = (String) request.get("userId");
        String bankId = (String)request.get("bankId");
        if (userId == null || bankId == null) return Result.error("获取题库的题目列表失败！");
        return questionBankService.getAllQuestionList(userId,bankId);
    }
    @PostMapping("/getQuestionById")
    public Result getQuestionById(@RequestBody Map<String,String> request) {
        String questionId = request.get("questionId");
        if (questionId == null) return Result.error("查询问题，问题ID为空！");
        return questionService.getQuestionById(questionId);
    }
    @PostMapping("/runTestCode")
    public Result runTestCode(@RequestBody Map<String,Object> request) {
        String testCode = (String)request.get("code");
        String questionId = (String)request.get("questionId");
        if (testCode == null || questionId == null)
            return Result.error("测试运行参数不可为空！");
        return questionService.runTestCode(questionId,testCode,0);
    }
    @PostMapping("/submitQuestion")
    public Result submitQuestion(@RequestBody Map<String,Object> request) {
        String questionId = (String)request.get("questionId");
        String userId = (String)request.get("userId");
        String code = (String)request.get("code");
        if (questionId == null || userId == null || code == null)
            return Result.error("提交题目参数不可为空！");
        return questionService.submitQuestion(userId,null,questionId,code);
    }
    @PostMapping("/addCollectBank")
    public Result addCollectBank(@RequestBody Map<String,Object> request) {
        String userId = request.get("userId").toString();
        String bankId = request.get("bankId").toString();
        Boolean collected = (Boolean) request.get("collected");
        System.out.println("collected"+collected);
        if (userId == null || bankId == null || collected == null)
            return Result.error("收藏参数不可以为空！");
        return questionBankService.addCollectBank(userId,bankId,collected);
    }
    @PostMapping("/addViewCount")
    public Result addViewCount(@RequestBody Map<String,Object> request) {
        String bankId = request.get("bankId").toString();
        if (bankId == null)
            return Result.error("更新参与次数的bankId不可为空！");
        return questionBankService.addViewCount(bankId);
    }
}
