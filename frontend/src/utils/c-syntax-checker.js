/**
 * C语言全量语法检测工具
 * 包含所有C语言基础语法规则检测，覆盖词法、语法、语义层面
 */
export const checkSyntax = {
    // 基础关键字（C89/C99核心）
    baseKeywords: [
        'auto', 'break', 'case', 'char', 'const', 'continue', 'default', 'do',
        'double', 'else', 'enum', 'extern', 'float', 'for', 'goto', 'if',
        'int', 'long', 'register', 'return', 'short', 'signed', 'sizeof', 'static',
        'struct', 'switch', 'typedef', 'union', 'unsigned', 'void', 'volatile', 'while'
    ],

    // 常用标准库函数
    standardFunctions: [
        'printf', 'scanf', 'getchar', 'putchar', 'gets', 'puts', 'fopen', 'fclose',
        'fread', 'fwrite', 'fscanf', 'fprintf', 'malloc', 'free', 'realloc', 'memset',
        'memcpy', 'strlen', 'strcpy', 'strcat', 'strcmp', 'strchr', 'strstr'
    ],

    // 头文件列表
    headerFiles: [
        'stdio.h', 'stdlib.h', 'string.h', 'math.h', 'ctype.h', 'time.h', 'stdbool.h'
    ],

    /**
     * 核心语法检测入口
     * @param {string} code - 待检测的C代码
     * @returns {Array<string>} 错误信息列表
     */
    checkAllSyntax(code) {
        const errors = [];
        const lines = code.split('\n').map(line => line.trim());

        // 1. 空代码检测
        if (!code.trim()) {
            errors.push('错误：代码不能为空');
            return errors;
        }

        // 2. 符号配对检测（核心）
        this.checkSymbolPairs(code, errors);

        // 3. 关键字与语法结构检测
        this.checkKeywordsAndStructure(code, lines, errors);

        // 4. 语句规范检测
        this.checkStatementRules(code, lines, errors);

        // 5. 数据类型与变量检测
        this.checkDataTypeAndVariables(code, lines, errors);

        // 6. 函数规范检测
        this.checkFunctionRules(code, errors);

        // 7. 注释规范检测
        this.checkCommentRules(code, errors);

        // 8. 禁止使用的语法（C++/非法语法）
        this.checkForbiddenSyntax(code, errors);

        return errors;
    },

    /**
     * 符号配对检测（括号、引号等）
     */
    checkSymbolPairs(code, errors) {
        const symbolPairs = [
            { open: '(', close: ')', name: '小括号' },
            { open: '{', close: '}', name: '大括号' },
            { open: '[', close: ']', name: '中括号' },
            { open: '"', close: '"', name: '双引号' },
            { open: '\'', close: '\'', name: '单引号' }
        ];

        symbolPairs.forEach(({ open, close, name }) => {
            const stack = [];
            let inString = false;
            let inChar = false;
            let inComment = false;

            for (let i = 0; i < code.length; i++) {
                const char = code[i];
                const nextChar = code[i + 1];
                const prevChar = code[i - 1];

                // 跳过注释内的符号检测
                if (char === '/' && nextChar === '*' && !inString && !inChar) {
                    inComment = true;
                    i++;
                    continue;
                }
                if (char === '*' && nextChar === '/' && inComment) {
                    inComment = false;
                    i++;
                    continue;
                }
                if (inComment) continue;

                // 跳过单行注释
                if (char === '/' && nextChar === '/') break;

                // 字符串/字符常量判断
                if (char === '"' && !inChar && !inComment) inString = !inString;
                if (char === '\'' && !inString && !inComment && prevChar !== '\\') inChar = !inChar;

                if (inString || inChar) continue;

                // 符号入栈/出栈
                if (char === open) {
                    stack.push({ pos: i, symbol: open });
                } else if (char === close) {
                    if (stack.length === 0) {
                        errors.push(`语法错误：多余的${name}闭合符 "${close}"（位置：${i+1}）`);
                    } else {
                        stack.pop();
                    }
                }
            }

            // 未闭合的符号
            if (stack.length > 0) {
                errors.push(`语法错误：${name}未闭合，剩余${stack.length}个未匹配的 "${open}"`);
            }
        });
    },

    /**
     * 关键字与核心结构检测
     */
    checkKeywordsAndStructure(code, lines, errors) {
        // 主函数检测
        const mainRegex = /int\s+main\s*\(\s*(void)?\s*\)/;
        if (!mainRegex.test(code)) {
            errors.push('语法错误：必须包含主函数 int main() 或 int main(void)');
        }

        // 主函数闭合检测
        const mainLines = code.match(/int\s+main[\s\S]*?\{[\s\S]*?\}/);
        if (!mainLines) {
            errors.push('语法错误：主函数体缺少 {} 包裹或未闭合');
        }

        // 头文件检测（至少包含一个标准头文件）
        const headerRegex = /#\s*include\s*<([a-zA-Z0-9_\.]+\.h)>/g;
        const matchedHeaders = code.match(headerRegex) || [];
        if (matchedHeaders.length === 0) {
            errors.push('语法错误：必须包含至少一个标准头文件（如 #include <stdio.h>）');
        } else {
            // 检测非法头文件
            matchedHeaders.forEach(header => {
                const headerName = header.match(/<([a-zA-Z0-9_\.]+\.h)>/)[1];
                if (!this.headerFiles.includes(headerName) && !headerName.startsWith('user_')) {
                    errors.push(`警告：使用非标准头文件 ${headerName}，可能无法编译`);
                }
            });
        }

        // 关键字拼写检测
        const codeLower = code.toLowerCase();
        this.baseKeywords.forEach(keyword => {
            const misspelled = this.getPossibleMisspellings(keyword);
            misspelled.forEach(mis => {
                if (codeLower.includes(mis) && !codeLower.includes(keyword)) {
                    errors.push(`疑似错误：可能将关键字 ${keyword} 拼写成了 ${mis}`);
                }
            });
        });
    },

    /**
     * 语句规范检测
     */
    checkStatementRules(code, lines, errors) {
        // 分号检测
        lines.forEach((line, idx) => {
            const lineNum = idx + 1;
            // 跳过空行、注释行、函数/结构定义行
            if (!line || line.startsWith('//') || line.startsWith('/*') || line.endsWith('{') ||
                line.match(/^\s*[a-zA-Z_]+[a-zA-Z0-9_]*\s*\(.*\)\s*\{/) ||
                line.match(/^\s*(if|for|while|switch)\s*\(.*\)$/)) {
                return;
            }

            // 检测语句末尾分号
            const cleanLine = line.replace(/\/\/.*/, '').replace(/".*?"/, '').replace(/'.*?'/, '').trim();
            if (cleanLine && !cleanLine.endsWith(';') && !cleanLine.endsWith('{') && !cleanLine.endsWith('}')) {
                errors.push(`语法错误：第${lineNum}行语句末尾缺少分号 ;`);
            }

            // 检测多余分号
            if (cleanLine.endsWith(';;')) {
                errors.push(`警告：第${lineNum}行存在多余的分号 ;;`);
            }
        });

        // if/else 语法检测
        const ifRegex = /if\s*\(.*\)\s*[^;{]/g;
        const ifMatches = code.match(ifRegex);
        if (ifMatches) {
            ifMatches.forEach(match => {
                errors.push(`语法错误：if 语句后缺少 { 或 分号（"${match.trim()}"）`);
            });
        }

        // for 循环语法检测
        const forRegex = /for\s*\(\s*[^;]*;\s*[^;]*;\s*[^)]*\)\s*[^;{]/g;
        const forMatches = code.match(forRegex);
        if (forMatches) {
            forMatches.forEach(match => {
                errors.push(`语法错误：for 循环后缺少 { 或 分号（"${match.trim()}"）`);
            });
        }
    },

    /**
     * 数据类型与变量检测
     */
    checkDataTypeAndVariables(code, lines, errors) {
        // 变量定义检测（未定义直接使用）
        const varRegex = /\b([a-zA-Z_][a-zA-Z0-9_]*)\b/g;
        const usedVars = new Set();
        const definedVars = new Set();

        // 提取已定义的变量
        lines.forEach(line => {
            // 匹配变量定义行（如 int a; char b = 'c';）
            const defRegex = /\b(int|char|float|double|long|short|unsigned|signed)\s+([a-zA-Z_][a-zA-Z0-9_]*)\b/;
            const defMatch = line.match(defRegex);
            if (defMatch) {
                definedVars.add(defMatch[2]);
            }
            // 匹配数组定义
            const arrRegex = /\b([a-zA-Z_][a-zA-Z0-9_]*)\s*\[\s*\d*\s*\]/;
            const arrMatch = line.match(arrRegex);
            if (arrMatch) {
                definedVars.add(arrMatch[1]);
            }
        });

        // 提取使用的变量（排除关键字、函数名）
        const allMatches = code.match(varRegex) || [];
        allMatches.forEach(token => {
            if (!this.baseKeywords.includes(token) &&
                !this.standardFunctions.includes(token) &&
                token !== 'main' &&
                !token.startsWith('__') &&
                !/^[0-9]+$/.test(token)) {
                usedVars.add(token);
            }
        });

        // 检测未定义变量
        usedVars.forEach(varName => {
            if (!definedVars.has(varName) && !varName.match(/^[A-Z_]+$/)) { // 排除宏定义
                errors.push(`语义错误：使用未定义的变量 ${varName}`);
            }
        });

        // 重复定义检测
        const varDefCounts = {};
        lines.forEach((line, idx) => {
            const defRegex = /\b(int|char|float|double)\s+([a-zA-Z_][a-zA-Z0-9_]*)\b/;
            const defMatch = line.match(defRegex);
            if (defMatch) {
                const varName = defMatch[2];
                varDefCounts[varName] = (varDefCounts[varName] || 0) + 1;
                if (varDefCounts[varName] > 1) {
                    errors.push(`语义错误：第${idx+1}行变量 ${varName} 重复定义`);
                }
            }
        });
    },

    /**
     * 函数规范检测
     */
    checkFunctionRules(code, errors) {
        // main函数返回值检测
        if (!code.match(/main\s*\(.*\)\s*\{[\s\S]*return\s+\d+;[\s\S]*\}/)) {
            errors.push('语法错误：main函数必须包含 return 0; 语句');
        }

        // 函数参数检测
        const funcRegex = /([a-zA-Z_][a-zA-Z0-9_]*)\s*\(([^)]*)\)/g;
        let funcMatch;
        while ((funcMatch = funcRegex.exec(code)) !== null) {
            const funcName = funcMatch[1];
            const params = funcMatch[2].trim();

            // main函数参数检测
            if (funcName === 'main' && params && params !== 'void') {
                errors.push(`警告：main函数参数建议为 void 或 int argc, char *argv[]（当前：${params}）`);
            }

            // 空参数检测
            if (params && !params.includes(' ') && params !== 'void') {
                errors.push(`语法错误：函数 ${funcName} 的参数格式错误（${params}），需指定类型`);
            }
        }

        // 标准函数调用检测
        this.standardFunctions.forEach(func => {
            const callRegex = new RegExp(`\\b${func}\\s*\\(`);
            if (code.match(callRegex)) {
                // printf/scanf 格式字符串检测
                if (func === 'printf' || func === 'scanf') {
                    const fmtRegex = new RegExp(`${func}\\s*\\("([^"]*)"`);
                    const fmtMatch = code.match(fmtRegex);
                    if (fmtMatch) {
                        const fmtStr = fmtMatch[1];
                        const placeholderCount = (fmtStr.match(/%[cdfseEfgGioulxXp]/g) || []).length;
                        const argCount = (fmtMatch.input.match(/\(/)[1].split(',').length - 1);

                        if (placeholderCount !== argCount) {
                            errors.push(`语义错误：${func} 格式符数量(${placeholderCount})与参数数量(${argCount})不匹配`);
                        }
                    }
                }
            }
        });
    },

    /**
     * 注释规范检测
     */
    checkCommentRules(code, errors) {
        // 多行注释未闭合
        const multiCommentRegex = /\/\*[\s\S]*?(?<!\*\/)$/;
        if (multiCommentRegex.test(code)) {
            errors.push('语法错误：多行注释 /* */ 未闭合');
        }

        // 注释嵌套检测
        const nestedCommentRegex = /\/\*[\s\S]*?\/\*[\s\S]*?\*\//;
        if (nestedCommentRegex.test(code)) {
            errors.push('语法错误：不允许多行注释嵌套 /* ... /* ... */ ... */');
        }
    },

    /**
     * 禁止使用的语法检测
     */
    checkForbiddenSyntax(code, errors) {
        // C++ 语法检测
        const cppPatterns = [
            { regex: /cout|cin/, msg: '禁止使用 C++ 输入输出（cout/cin），请使用 printf/scanf' },
            { regex: /using\s+namespace\s+std/, msg: '禁止使用 C++ 命名空间（using namespace std）' },
            { regex: /new\s*\[|\]\s*delete/, msg: '禁止使用 C++ 内存管理（new/delete），请使用 malloc/free' },
            { regex: /bool|true|false/, msg: 'C89 标准不支持 bool/true/false，如需使用请包含 <stdbool.h>' },
            { regex: /::/, msg: '禁止使用 C++ 作用域解析符 ::' }
        ];

        cppPatterns.forEach(({ regex, msg }) => {
            if (regex.test(code)) {
                errors.push(`语法错误：${msg}`);
            }
        });

        // 危险函数检测
        const dangerousFuncs = [
            { func: 'gets', msg: '禁止使用危险函数 gets()，易导致缓冲区溢出，请使用 fgets()' },
            { func: 'system', msg: '禁止使用系统调用函数 system()，存在安全风险' },
            { func: 'goto', msg: '不建议使用 goto 语句，易导致代码可读性差' }
        ];

        dangerousFuncs.forEach(({ func, msg }) => {
            const regex = new RegExp(`\\b${func}\\s*\\(`);
            if (regex.test(code)) {
                errors.push(`警告：${msg}`);
            }
        });
    },

    /**
     * 获取关键字可能的拼写错误
     * @param {string} keyword - 正确关键字
     * @returns {Array<string>} 可能的错误拼写
     */
    getPossibleMisspellings(keyword) {
        const misspellings = {
            'int': ['itn', 'inet'],
            'char': ['chra', 'carh'],
            'float': ['flaot', 'floa'],
            'double': ['doubel', 'doulbe'],
            'if': ['fi'],
            'else': ['els', 'esle'],
            'for': ['rof'],
            'while': ['wihle', 'whiel'],
            'return': ['retrun', 'retunr'],
            'break': ['brek', 'braek'],
            'continue': ['contiune', 'conitnue'],
            'switch': ['swtich', 'switchh'],
            'case': ['cas', 'caes'],
            'default': ['defualt', 'deafult'],
            'struct': ['strcut', 'sturct'],
            'union': ['unoin', 'uion'],
            'typedef': ['typedeff', 'tyepdef']
        };
        return misspellings[keyword] || [];
    }
};

// const checkSyntax = async () => {
//     try {
//         syntaxErrors.value = []
//
//         if (!currentCode.value || currentCode.value.trim() === '') {
//             syntaxErrors.value.push('错误：代码不能为空')
//             return
//         }
//
//         // 检查必要的C语言元素
//         if (!currentCode.value.includes('main(')) {
//             syntaxErrors.value.push('错误：缺少 main 函数')
//         }
//
//         if (!currentCode.value.includes('#')) {
//             syntaxErrors.value.push('警告：建议包含必要的头文件')
//         }
//
//         // 括号匹配检查
//         const stack = []
//         let lineNumber = 1
//
//         for (let i = 0; i < currentCode.value.length; i++) {
//             const char = currentCode.value[i]
//             if (char === '\n') {
//                 lineNumber++
//                 continue
//             }
//             if (char === '{' || char === '(' || char === '[') {
//                 stack.push({ char, line: lineNumber })
//             } else if (char === '}') {
//                 const last = stack.pop()
//                 if (!last || last.char !== '{') {
//                     syntaxErrors.value.push(`错误：第 ${lineNumber} 行多余的 "}"`)
//                 }
//             } else if (char === ')') {
//                 const last = stack.pop()
//                 if (!last || last.char !== '(') {
//                     syntaxErrors.value.push(`错误：第 ${lineNumber} 行多余的 ")"`)
//                 }
//             } else if (char === ']') {
//                 const last = stack.pop()
//                 if (!last || last.char !== '[') {
//                     syntaxErrors.value.push(`错误：第 ${lineNumber} 行多余的 "]"`)
//                 }
//             }
//         }
//
//         // 检查未闭合的括号
//         if (stack.length > 0) {
//             stack.forEach(item => {
//                 syntaxErrors.value.push(`错误：第 ${item.line} 行的 "${item.char}" 未闭合`)
//             })
//         }
//
//         // 检查分号
//         const lines = currentCode.value.split('\n')
//         lines.forEach((line, index) => {
//             const trimmed = line.trim()
//             if (trimmed && !trimmed.startsWith('#') &&
//                 !trimmed.startsWith('//') &&
//                 !trimmed.startsWith('/*') &&
//                 !trimmed.endsWith('{') &&
//                 !trimmed.endsWith('}') &&
//                 !trimmed.endsWith(';') &&
//                 !trimmed.endsWith(')') &&
//                 !trimmed.includes('for') &&
//                 !trimmed.includes('if') &&
//                 !trimmed.includes('while')) {
//                 if (trimmed.length > 0 && !trimmed.endsWith(';')) {
//                     syntaxErrors.value.push(`警告：第 ${index + 1} 行可能缺少分号`)
//                 }
//             }
//         })
//
//     } catch (error) {
//         console.error('语法检查失败：', error)
//         syntaxErrors.value = ['错误：语法检查异常']
//     }
// }