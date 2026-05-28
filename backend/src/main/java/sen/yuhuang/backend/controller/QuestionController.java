package sen.yuhuang.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.service.QuestionService;

import java.util.Map;

@RestController
@RequestMapping("/api/exam")
public class QuestionController {
    @Autowired
    private QuestionService questionService;

    @PostMapping("/getExamQuestion")
    public Result getExamQuestion(@RequestBody Map<String,String> request){
        String examId = request.get("examId");
        if (examId == null)return Result.badRequest("获取考试问题失败！");
        return questionService.getExamQuestion(examId);
    }

    @PostMapping("/submitQuestion")
    public Result submitQuestion(@RequestBody Map<String,String> request){
        String examId = request.get("examId");
        String userId = request.get("userId");
        String questionId = request.get("questionId");
        String code =  request.get("code");

        if (userId == null || examId == null || questionId == null || code == null) return Result.badRequest("提交问题参数不可以为空！");
        return questionService.submitQuestion(userId,examId,questionId,code);
    }
    @PostMapping("/runTestCode")
    public Result runTestCode(@RequestBody Map<String,String> request){
        String code = request.get("code");
        String questionId = request.get("questionId");
        if (questionId == null) return Result.error("测试题目ID不可以为空");
        if (code == null) return Result.error("代码不可以为空!");
        return questionService.runTestCode(questionId,code,1);
    }



}
