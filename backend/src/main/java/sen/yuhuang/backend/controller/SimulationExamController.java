package sen.yuhuang.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.dto.ExamStatusUpdateDTO;
import sen.yuhuang.backend.entity.SimulationExam;
import sen.yuhuang.backend.service.SimulationExamResultService;
import sen.yuhuang.backend.service.SimulationExamService;
import sen.yuhuang.backend.service.ViolationRecordService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam")
public class SimulationExamController {
    @Autowired
    SimulationExamService simulationExamService;

    @Autowired
    SimulationExamResultService  simulationExamResultService;

    @Autowired
    ViolationRecordService violationRecordService;

    @PostMapping("/getExamList")
    public Result getExamList(@RequestBody Map<String,String> request){
        String roleId = request.get("roleId");
        if (roleId == null) return Result.badRequest("roleId不可以为空！");

        return simulationExamService.getExamList(roleId);
    }
    @PostMapping("/getExamByExamId")
    public Result getExamByExamId(@RequestBody Map<String,String> request){
        String examId = request.get("examId");
        if (examId.equals("") || examId == null) return Result.badRequest("examId不合法！");
        return simulationExamService.getExamByExamId(examId);
    }
    @PostMapping("/violation")
    public Result violation(@RequestBody Map<String,String> request){
        String examId = request.get("examId");
        String userId = request.get("userId");
        String violationType =  request.get("violationType");
        String count = request.get("count");
        if (examId == null || userId == null || violationType == null || count == null)
            return Result.badRequest("违规记录请求参数异常!");
        return violationRecordService.addViolationRecord(examId,userId,violationType,count);
    }
    @PostMapping("/submitExam")
    public Result submitExam(@RequestBody Map<String,String> request){
        String examId = request.get("examId");
        String userId = request.get("userId");
        String scoreTotal = request.get("scoreTotal");
        if(examId == null || userId == null || scoreTotal == null){
            return Result.error("交卷参数异常！");
        }
        return simulationExamService.submitExam(examId,userId,scoreTotal);
    }

    @PostMapping("/batchUpdateStatus")
    public Result batchUpdateExamStatus(@RequestBody List<ExamStatusUpdateDTO> updates) {
        try {
            for (ExamStatusUpdateDTO update : updates) {
                simulationExamService.updateExamStatus(update.getId(), update.getNewStatus());
            }
            return Result.ok().setMessage("状态更新成功");
        } catch (Exception e) {
            return Result.error("状态更新失败");
        }

    }

    @PostMapping("/progress")
    public Result saveProgress(@RequestBody Map<String,Object> request) {
        return simulationExamService.saveProgress(request);
    }

    @GetMapping("/{examId}/progress")
    public Result getProgress(@PathVariable String examId, @RequestParam String userId) {
        if (examId == null || examId.trim().isEmpty()) return Result.badRequest("examId不可以为空！");
        if (userId == null || userId.trim().isEmpty()) return Result.badRequest("userId不可以为空！");
        return simulationExamService.getProgress(userId, examId);
    }
}
