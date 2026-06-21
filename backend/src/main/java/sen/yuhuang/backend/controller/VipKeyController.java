package sen.yuhuang.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.service.VipKeyService;

import java.util.Map;

@RestController
@RequestMapping("/api/vip/key")
public class VipKeyController {

    @Autowired
    private VipKeyService vipKeyService;

    @PostMapping("/generate")
    public Result generateKeys(@RequestBody Map<String, Object> request) {
        return vipKeyService.generateKeys(request);
    }

    @PostMapping("/list")
    public Result listKeys(@RequestBody Map<String, Object> request) {
        return vipKeyService.listKeys(request);
    }

    @PostMapping("/redeem")
    public Result redeemKey(@RequestBody Map<String, Object> request) {
        return vipKeyService.redeemKey(request);
    }

    @PostMapping("/delete")
    public Result deleteKey(@RequestBody Map<String, Object> request) {
        return vipKeyService.deleteKey(request);
    }
}
