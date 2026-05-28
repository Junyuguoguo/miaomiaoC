package sen.yuhuang.backend.controller.teacher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.service.QuestionBankService;

import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class BankManagerController {
    @Autowired
    QuestionBankService questionBankService;

    @PostMapping("/loadBankList")
    public Result loadBankList(){
        return questionBankService.loadBankList();
    }

    @PostMapping("/saveBank")
    public Result saveBank(@RequestBody Map<String, Object> request) {
        return questionBankService.saveBank(request);
    }

    @PostMapping("/deleteBank")
    public Result deleteBank(@RequestBody Map<String, Object> request) {
        Object bankId = request.get("id");
        if (bankId == null || bankId.toString().trim().isEmpty()) {
            return Result.error("题库ID不能为空");
        }
        return questionBankService.deleteBank(bankId.toString());
    }
}
