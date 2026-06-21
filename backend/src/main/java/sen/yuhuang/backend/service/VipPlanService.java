package sen.yuhuang.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.entity.VipPlan;
import sen.yuhuang.backend.repository.VipPlanRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class VipPlanService {

    @Autowired
    private VipPlanRepository vipPlanRepository;

    public Result listEnabledPlans() {
        ensureDefaultPlans();
        return Result.ok(vipPlanRepository.findByEnabledOrderBySortOrderAscIdAsc(1));
    }

    public Result listAllPlans() {
        ensureDefaultPlans();
        return Result.ok(vipPlanRepository.findAllByOrderBySortOrderAscIdAsc());
    }

    @Transactional
    public Result savePlan(Map<String, Object> request) {
        try {
            String idText = toText(request.get("id"));
            String planName = toText(request.get("planName"));
            String priceText = toText(request.get("price"));
            String durationDaysText = toText(request.get("durationDays"));

            if (planName == null || planName.isEmpty()) {
                return Result.badRequest("套餐名称不能为空");
            }
            if (priceText == null || priceText.isEmpty()) {
                return Result.badRequest("VIP价格不能为空");
            }
            if (durationDaysText == null || durationDaysText.isEmpty()) {
                return Result.badRequest("有效天数不能为空");
            }

            BigDecimal price = new BigDecimal(priceText);
            Integer durationDays = Integer.parseInt(durationDaysText);
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                return Result.badRequest("VIP价格不能小于0");
            }
            if (durationDays <= 0) {
                return Result.badRequest("有效天数必须大于0");
            }

            VipPlan vipPlan;
            if (idText != null && !idText.isEmpty()) {
                Long id = Long.valueOf(idText);
                vipPlan = vipPlanRepository.findById(id).orElse(null);
                if (vipPlan == null) {
                    return Result.notFound("VIP套餐不存在");
                }
            } else {
                vipPlan = new VipPlan();
            }

            vipPlan.setPlanName(planName);
            vipPlan.setPrice(price);
            vipPlan.setDurationDays(durationDays);
            vipPlan.setContactQq(defaultText(request.get("contactQq")));
            vipPlan.setContactWechat(defaultText(request.get("contactWechat")));
            vipPlan.setContactNote(defaultText(request.get("contactNote")));
            vipPlan.setBenefits(defaultText(request.get("benefits")));
            vipPlan.setEnabled(parseInteger(request.get("enabled"), 1));
            vipPlan.setSortOrder(parseInteger(request.get("sortOrder"), 0));

            return Result.ok(vipPlanRepository.save(vipPlan));
        } catch (NumberFormatException e) {
            return Result.badRequest("价格或天数格式不正确");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("保存VIP套餐失败：" + e.getMessage());
        }
    }

    @Transactional
    public Result deletePlan(Map<String, Object> request) {
        try {
            String idText = toText(request.get("id"));
            if (idText == null || idText.isEmpty()) {
                return Result.badRequest("VIP套餐ID不能为空");
            }
            Long id = Long.valueOf(idText);
            if (!vipPlanRepository.existsById(id)) {
                return Result.notFound("VIP套餐不存在");
            }
            vipPlanRepository.deleteById(id);
            return Result.ok().setMessage("删除VIP套餐成功");
        } catch (NumberFormatException e) {
            return Result.badRequest("VIP套餐ID格式不正确");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除VIP套餐失败：" + e.getMessage());
        }
    }

    private void ensureDefaultPlans() {
        if (vipPlanRepository.count() > 0) {
            return;
        }

        List<VipPlan> defaults = new ArrayList<>();
        defaults.add(buildDefaultPlan("月度VIP", "29.90", 30, 1));
        defaults.add(buildDefaultPlan("季度VIP", "79.90", 90, 2));
        defaults.add(buildDefaultPlan("年度VIP", "199.00", 365, 3));
        vipPlanRepository.saveAll(defaults);
    }

    private VipPlan buildDefaultPlan(String planName, String price, Integer durationDays, Integer sortOrder) {
        VipPlan vipPlan = new VipPlan();
        vipPlan.setPlanName(planName);
        vipPlan.setPrice(new BigDecimal(price));
        vipPlan.setDurationDays(durationDays);
        vipPlan.setContactQq("后台待设置");
        vipPlan.setContactWechat("后台待设置");
        vipPlan.setContactNote("请联系管理员或教师确认付款方式，开通后会手动升级账号。");
        vipPlan.setBenefits("解锁VIP考试、VIP题库、重点练习资料和后续新增会员内容。");
        vipPlan.setEnabled(1);
        vipPlan.setSortOrder(sortOrder);
        return vipPlan;
    }

    private Integer parseInteger(Object value, Integer defaultValue) {
        String text = toText(value);
        return text == null ? defaultValue : Integer.parseInt(text);
    }

    private String defaultText(Object value) {
        String text = toText(value);
        return text == null ? "" : text;
    }

    private String toText(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text) || "undefined".equalsIgnoreCase(text)) {
            return null;
        }
        return text;
    }
}
