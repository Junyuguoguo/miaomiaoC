package sen.yuhuang.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.entity.SimulationExam;
import sen.yuhuang.backend.entity.User;
import sen.yuhuang.backend.entity.ViolationRecord;
import sen.yuhuang.backend.repository.SimulationExamRepository;
import sen.yuhuang.backend.repository.UserRepository;
import sen.yuhuang.backend.repository.ViolationRecordRepository;

@Service
public class ViolationRecordService {
    @Autowired
    SimulationExamRepository simulationExamRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ViolationRecordRepository violationRecordRepository;

    public Result addViolationRecord(String examId, String userId, String violationType, String count) {
        try {
            //确认examId是否存在
            SimulationExam exam = simulationExamRepository.getExamByExamId(Long.valueOf(examId));
            if (exam == null) return Result.badRequest("该场考试不存在！");

            User user = userRepository.findUserByUserId(Long.valueOf(userId));
            if (user == null) return Result.badRequest("违规用户不存在！");

            ViolationRecord violationRecord = ViolationRecord.builder()
                    .examId(Long.valueOf(examId))
                    .userId(Long.valueOf(userId))
                    .violationDesc("违规类型:"+violationType+" 次数:"+count)
                    .build();
            violationRecordRepository.save(violationRecord);
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.badRequest(e.getMessage());
        }

    }
}
