# miaomiaoC2 endpoint inventory

这是从前端打包产物和 Spring Boot jar 反推出来的接口清单。路径里出现的双斜杠已按正常访问方式归一化为单斜杠。

| Method | Path | Controller method |
|---|---|---|
| POST | /api/auth/login | login |
| POST | /api/auth/logout | logout |
| POST | /api/auth/register | register |
| POST | /api/auth/sendEmail | sendEmail |
| PUT | /api/auth/nextResetPassWord | nextResetPassWord |
| PUT | /api/auth/resetPassword | resetPassword |
| POST | /api/auth/verifyToken | verifyToken |
| POST | /api/auth/getUserInfo | getUserInfo |
| PUT | /api/auth/updateUserInfo | updateUserInfo |
| POST | /api/auth/getStatsData | getStatsData |
| POST | /api/bank/getBankList | getBankList |
| POST | /api/bank/getCollectBankIds | getCollectBankIds |
| POST | /api/bank/getWrongBankCount | getWrongBankCount |
| POST | /api/bank/getAllQuestionList | getAllQuestionList |
| POST | /api/bank/getQuestionById | getQuestionById |
| POST | /api/bank/runTestCode | runTestCode |
| POST | /api/bank/submitQuestion | submitQuestion |
| POST | /api/bank/addCollectBank | addCollectBank |
| POST | /api/bank/addViewCount | addViewCount |
| POST | /api/bank/getWrongQuestionList | getWrongQuestionList |
| POST | /api/bank/removeWrongQuestion | removeWrongQuestion |
| POST | /api/exam/getExamList | getExamList |
| POST | /api/exam/getExamByExamId | getExamByExamId |
| POST | /api/exam/getExamQuestion | getExamQuestion |
| POST | /api/exam/submitQuestion | submitQuestion |
| POST | /api/exam/runTestCode | runTestCode |
| POST | /api/exam/violation | violation |
| POST | /api/exam/violations | violations |
| POST | /api/exam/progress | saveProgress |
| GET | /api/exam/{examId}/progress | getProgress |
| POST | /api/exam/submitExam | submitExam |
| POST | /api/exam/getExamRecordList | getExamRecordList |
| POST | /api/exam/getExamResultById | getExamResultById |
| POST | /api/exam/batchUpdateStatus | batchUpdateExamStatus |
| POST | /api/teacher/loadBankList | loadBankList |
| POST | /api/teacher/saveBank | saveBank |
| POST | /api/teacher/deleteBank | deleteBank |
| POST | /api/teacher/loadExamData | loadExamData |
| POST | /api/teacher/saveExam | saveExam |
| POST | /api/teacher/deleteExam | deleteExam |
| POST | /api/teacher/loadQuestionList | loadQuestionList |
| POST | /api/teacher/addQuestion | addQuestion |
| POST | /api/teacher/updateQuestion | updateQuestion |
| POST | /api/teacher/deleteQ | deleteQuestion |
| GET | /api/vip/plans | listEnabledPlans |
| GET | /api/vip/manage/plans | listAllPlans |
| POST | /api/vip/manage/save | savePlan |
| POST | /api/vip/manage/delete | deletePlan |
| POST | /api/vip/key/generate | generateKeys |
| POST | /api/vip/key/list | listKeys |
| POST | /api/vip/key/redeem | redeemKey |
| POST | /api/vip/key/delete | deleteKey |
