package sen.yuhuang.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.service.UserWrongQuestionService;

import java.util.Map;

@RestController
@RequestMapping("/api/bank/")
public class UserWrongQuestionController {

    @Autowired
    private UserWrongQuestionService userWrongQuestionService;

    @PostMapping("/getWrongQuestionList")
    public Result getWrongQuestionList(@RequestBody Map<String,Object> request) {
        String userId = (String) request.get("userId");
        if (userId == null) return Result.error("获取错题本参数异常！");

        return userWrongQuestionService.getWrongQuestionList(userId);

    }
    @PostMapping("/removeWrongQuestion")
    public Result removeWrongQuestion(@RequestBody Map<String,Object> request) {
        String wrongId =   request.get("wrongId").toString();
        String userId = request.get("userId").toString();
        if (wrongId == null || userId == null)
            return Result.error("移除错题失败，参数不可为空！");
        return userWrongQuestionService.removeWrongQuestion(userId,wrongId);
    }
}
