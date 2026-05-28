package sen.yuhuang.backend.dto;

import lombok.Data;


@Data
public class QuestionBankDTO {
    private Long id;              // 题库ID
    private String title;         // 题库标题
    private String desc;          // 题库描述
    private String level;         // 难度等级（入门、简单、中等、困难）
    private Integer questionCount; // 题目数量
    private Integer recommend;     // 推荐度（0-100）
    private Integer viewCount;     // 学习人数
    private Boolean collected;     // 是否收藏（重要：布尔类型）
    private Integer wrongCount;    // 错题数量（错题本用）
    private Integer isVip;         // 是否VIP题库（0-普通，1-VIP）
}