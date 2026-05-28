import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import fs from 'fs'

// 手动读取环境变量文件
function loadEnvFile(mode) {
    const envPath = path.resolve(process.cwd(), 'env', `.env.${mode}`)
    console.log('尝试读取文件:', envPath)

    try {
        if (!fs.existsSync(envPath)) {
            console.log(`文件不存在: ${envPath}`)
            return {}
        }

        const envContent = fs.readFileSync(envPath, 'utf-8')
        console.log('文件内容:', envContent)

        const env = {}
        envContent.split('\n').forEach(line => {
            // 去除首尾空格
            line = line.trim()
            // 跳过注释行和空行
            if (line && !line.startsWith('#')) {
                const equalIndex = line.indexOf('=')
                if (equalIndex > 0) {
                    const key = line.substring(0, equalIndex).trim()
                    const value = line.substring(equalIndex + 1).trim()
                    env[key] = value
                    console.log(`解析变量: ${key}=${value}`)
                }
            }
        })
        return env
    } catch (error) {
        console.error('读取环境变量文件失败:', error)
        return {}
    }
}

export default defineConfig(({ mode }) => {
    const isProduction = mode === 'production'

    // 手动读取环境变量
    const env = loadEnvFile(mode)

    console.log('========== 构建信息 ==========')
    console.log('当前模式:', mode)
    console.log('是否生产环境:', isProduction)
    console.log('VITE_API_BASE_URL:', env.VITE_API_BASE_URL)
    console.log('================================')

    // 定义要注入的环境变量
    const defineEnv = {}
    Object.keys(env).forEach(key => {
        if (key.startsWith('VITE_')) {
            defineEnv[`import.meta.env.${key}`] = JSON.stringify(env[key])
        }
    })

    return {
        base: isProduction ? '/' : '/',
        plugins: [vue()],

        // 手动定义环境变量
        define: defineEnv,

        resolve: {
            alias: {
                '@': path.resolve(__dirname, './src')
            },
            dedupe: ['@codemirror/state', '@codemirror/view'],
            extensions: ['.mjs', '.js', '.jsx', '.json', '.vue']
        },

        optimizeDeps: {
            include: ['@codemirror/state', '@codemirror/view', '@codemirror/lang-cpp'],
            force: true
        },

        server: {
            port: 5173,
            host: '0.0.0.0',
            proxy: {
                '/api': {
                    target: env.VITE_API_BASE_URL || 'http://localhost:8080',
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/api/, '')
                }
            }
        },

        build: {
            outDir: 'dist',
            assetsDir: 'assets',
            minify: 'terser',
            terserOptions: {
                compress: {
                    drop_console: isProduction,
                    drop_debugger: isProduction
                }
            },
            rollupOptions: {
                output: {
                    chunkFileNames: 'js/[name]-[hash].js',
                    entryFileNames: 'js/[name]-[hash].js',
                    assetFileNames: '[ext]/[name]-[hash].[ext]'
                }
            }
        },

        envDir: path.resolve(__dirname, './env')
    }
})