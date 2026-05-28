package sen.yuhuang.backend.controller.teacher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.service.SimulationExamService;

import java.util.Map;

@RestController
@RequestMapping("/api/teacher/")
public class ExamManagerController {
    @Autowired
    SimulationExamService simulationExamService;

    @PostMapping("/loadExamData")
    public Result loadExamData(){
        return simulationExamService.loadExamData();
    }

    @PostMapping("/saveExam")
    public Result saveExam(@RequestBody Map<String, Object> request) {
        return simulationExamService.saveExam(request);
    }

    @PostMapping("/deleteExam")
    public Result deleteExam(@RequestBody Map<String, Object> request) {
        Object examId = request.get("id");
        if (examId == null || examId.toString().trim().isEmpty()) {
            return Result.error("考试ID不能为空");
        }
        return simulationExamService.deleteExam(examId.toString());
    }
}
