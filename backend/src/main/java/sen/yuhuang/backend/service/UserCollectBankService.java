package sen.yuhuang.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.repository.UserCollectBankRepository;

import java.util.List;

@Service
public class UserCollectBankService {

    @Autowired
    UserCollectBankRepository userCollectBankRepository;

    public Result getCollectBankIds(String userId) {
        try {
            Long userIdLong =  Long.parseLong(userId);
            List<Long> bankIds = userCollectBankRepository.findBankIdsByUserId(userIdLong);
            return Result.ok(bankIds);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取收藏列表失败：" + e.getMessage());
        }
    }
}
