package sen.yuhuang.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.service.SimulationExamResultService;

import java.util.Map;

@RestController
@RequestMapping("/api/exam")
public class SimulationExamResultController {
    @Autowired
    SimulationExamResultService simulationExamResultService;

    @PostMapping("/getExamRecordList")
    public Result getExamRecordList(@RequestBody Map<String,String> request){
        String userId = request.get("userId");
        if(userId == null || userId.equals("")) return Result.error("加载考试记录中，UserId不可为空！");
        return simulationExamResultService.getExamRecordList(userId);
    }

    @PostMapping("/getExamResultById")
    public Result getExamResultById(@RequestBody Map<String,String> request){
        String userId = request.get("userId");
        String examId = request.get("examId");
        if(userId == null || userId.equals("")) return Result.error("加载考试结果中，UserId不可为空！");
        if(examId == null || examId.equals("")) return Result.error("加载考试结果中，examId不可为空！");
        return simulationExamResultService.getExamResultById(userId, examId);
    }
}
