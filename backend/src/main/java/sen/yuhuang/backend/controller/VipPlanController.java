package sen.yuhuang.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.service.VipPlanService;

import java.util.Map;

@RestController
@RequestMapping("/api/vip")
public class VipPlanController {

    @Autowired
    private VipPlanService vipPlanService;

    @GetMapping("/plans")
    public Result listEnabledPlans() {
        return vipPlanService.listEnabledPlans();
    }

    @GetMapping("/manage/plans")
    public Result listAllPlans() {
        return vipPlanService.listAllPlans();
    }

    @PostMapping("/manage/save")
    public Result savePlan(@RequestBody Map<String, Object> request) {
        return vipPlanService.savePlan(request);
    }

    @PostMapping("/manage/delete")
    public Result deletePlan(@RequestBody Map<String, Object> request) {
        return vipPlanService.deletePlan(request);
    }
}
