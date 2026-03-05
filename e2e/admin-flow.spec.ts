import { test, expect } from '@playwright/test';

test.describe('管理员流程 E2E 测试', () => {
  test.beforeEach(async ({ page }) => {
    // 登录管理员账户
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');

    // 等待跳转到管理员页面
    await page.waitForURL('/admin');
  });

  test('LLM 配置管理流程', async ({ page }) => {
    // 1. 验证管理员页面加载
    await expect(page).toHaveTitle(/管理员控制台/);
    await expect(page.getByText('管理员控制台')).toBeVisible();

    // 2. 验证 AI 配置标签页
    await expect(page.getByText('AI 配置')).toBeVisible();
    await expect(page.getByText('LLM 配置')).toBeVisible();

    // 3. 点击测试连接按钮
    await page.click('text=测试连接');
    await expect(page.getByText('测试中...')).toBeVisible();

    // 等待测试完成
    await page.waitForTimeout(2000);
    await expect(page.getByText('连接成功')).toBeVisible();

    // 4. 修改配置
    await page.fill('input[placeholder="https://api.openai.com/v1"]', 'https://new-api.example.com/v1');
    await page.fill('input[placeholder="sk-********************************"]', 'new-api-key');
    await page.fill('input[placeholder="gpt-3.5-turbo"]', 'gpt-4');

    // 5. 保存配置
    await page.click('text=保存配置');
    await expect(page.getByText('保存中...')).toBeVisible();

    // 等待保存完成
    await page.waitForTimeout(1000);
    await expect(page.getByText('配置保存成功')).toBeVisible();

    // 6. 验证配置已更新
    await expect(page.getByDisplayValue('https://new-api.example.com/v1')).toBeVisible();
    await expect(page.getByDisplayValue('new-api-key')).toBeVisible();
    await expect(page.getByDisplayValue('gpt-4')).toBeVisible();
  });

  test('用户管理流程', async ({ page }) => {
    // 1. 切换到用户管理标签
    await page.click('text=用户管理');

    // 等待用户列表加载
    await expect(page.getByText('用户列表')).toBeVisible();
    await expect(page.getByText('账户信息 + 订单信息')).toBeVisible();

    // 2. 验证用户列表显示
    await expect(page.getByText('haight_user')).toBeVisible();
    await expect(page.getByText('user@haightcafe.com')).toBeVisible();
    await expect(page.getByText('¥150.00')).toBeVisible();

    // 3. 搜索用户
    await page.fill('input[placeholder="搜索用户名或邮箱..."]', 'emma');
    await page.click('text=搜索');

    // 等待搜索结果
    await expect(page.getByText('emma_bean')).toBeVisible();
    await expect(page.getByText('lover@haightcafe.com')).toBeHidden();

    // 4. 查看用户详情
    await page.click('text=查看');
    await expect(page.getByText('用户详情')).toBeVisible();
    await expect(page.getByText('haight_user')).toBeVisible();
    await expect(page.getByText('user@haightcafe.com')).toBeVisible();
    await expect(page.getByText('+1 (415) 555-0101')).toBeVisible();

    // 关闭详情弹窗
    await page.click('text=关闭');
    await expect(page.getByText('用户详情')).toBeHidden();

    // 5. 封禁用户
    await page.click('text=封禁');
    await page.click('text=确定');

    // 验证封禁状态
    await expect(page.getByText('封禁')).toBeVisible();
    await expect(page.getByText('活跃')).toBeHidden();

    // 6. 解封用户
    await page.click('text=解封');
    await page.click('text=确定');

    // 验证解封状态
    await expect(page.getByText('活跃')).toBeVisible();
    await expect(page.getByText('封禁')).toBeHidden();
  });

  test('LLM 配置完整流程', async ({ page }) => {
    // 1. 添加新配置
    await page.click('text=LLM 配置');

    // 模拟点击"添加配置"按钮（如果有）
    // await page.click('text=添加配置');

    // 2. 填写配置信息
    await page.fill('input[placeholder="https://api.modelscope.cn/v1"]', 'https://api.modelscope.cn/v1');
    await page.fill('input[placeholder="sk-********************************"]', 'modelscope-api-key');
    await page.fill('input[placeholder="Qwen/Qwen2.5-Coder-Instruct"]', 'Qwen/Qwen2.5-Coder-Instruct');
    await page.fill('input[placeholder="0.7"]', '0.8');
    await page.fill('input[placeholder="1000"]', '1500');

    // 3. 测试连接
    await page.click('text=测试连接');
    await expect(page.getByText('测试中...')).toBeVisible();
    await page.waitForTimeout(2000);
    await expect(page.getByText('连接成功')).toBeVisible();

    // 4. 保存配置
    await page.click('text=保存配置');
    await expect(page.getByText('保存中...')).toBeVisible();
    await page.waitForTimeout(1000);
    await expect(page.getByText('配置保存成功')).toBeVisible();

    // 5. 验证配置已保存到列表
    await expect(page.getByText('Modelscope')).toBeVisible();
    await expect(page.getByText('Qwen/Qwen2.5-Coder-Instruct')).toBeVisible();

    // 6. 编辑配置
    await page.click('text=编辑');

    // 修改配置
    await page.fill('input[placeholder="Qwen/Qwen2.5-Coder-Instruct"]', 'Qwen/Qwen2.5-Coder-Instruct-updated');

    // 保存修改
    await page.click('text=保存配置');
    await expect(page.getByText('配置保存成功')).toBeVisible();

    // 7. 删除配置
    await page.click('text=删除');
    await page.click('text=确定');

    // 验证配置已删除
    await expect(page.getByText('Modelscope')).toBeHidden();
  });

  test('错误处理流程', async ({ page }) => {
    // 1. 测试无效配置保存
    await page.fill('input[placeholder="sk-********************************"]', '');
    await page.click('text=保存配置');

    // 验证错误提示
    await expect(page.getByText('配置保存失败')).toBeVisible();

    // 2. 测试连接失败
    await page.fill('input[placeholder="sk-********************************"]', 'invalid-api-key');
    await page.click('text=测试连接');
    await expect(page.getByText('测试中...')).toBeVisible();
    await page.waitForTimeout(2000);
    await expect(page.getByText('连接失败')).toBeVisible();

    // 3. 测试 API 错误处理
    await page.fill('input[placeholder="sk-********************************"]', 'valid-api-key');
    await page.click('text=测试连接');
    await expect(page.getByText('连接成功')).toBeVisible();
  });

  test('权限验证', async ({ page }) => {
    // 1. 尝试直接访问管理员页面
    await page.goto('/admin');

    // 应该被重定向到登录页面
    await expect(page).toHaveURL('/login');

    // 2. 登录普通用户
    await page.fill('input[name="username"]', 'normal_user');
    await page.fill('input[name="password"]', 'password123');
    await page.click('button[type="submit"]');

    // 3. 尝试访问管理员页面
    await page.goto('/admin');

    // 应该被拒绝或重定向
    await expect(page).not.toHaveURL('/admin');
  });

  test('响应式设计', async ({ page }) => {
    // 1. 测试桌面视图
    await page.setViewportSize({ width: 1280, height: 720 });
    await expect(page.getByText('管理员控制台')).toBeVisible();

    // 2. 测试平板视图
    await page.setViewportSize({ width: 768, height: 1024 });
    await expect(page.getByText('管理员控制台')).toBeVisible();

    // 3. 测试移动视图
    await page.setViewportSize({ width: 375, height: 667 });
    await expect(page.getByText('管理员控制台')).toBeVisible();

    // 验证标签导航在移动设备上正常工作
    await page.click('text=用户管理');
    await expect(page.getByText('用户列表')).toBeVisible();
  });

  test('性能测试', async ({ page }) => {
    // 1. 测试页面加载时间
    const startTime = Date.now();
    await page.goto('/admin');
    const loadTime = Date.now() - startTime;

    // 页面加载时间应该小于 3 秒
    expect(loadTime).toBeLessThan(3000);

    // 2. 测试配置保存时间
    await page.click('text=测试连接');
    const testStart = Date.now();
    await page.waitForSelector('text=测试中...');
    await page.waitForSelector('text=连接成功', { state: 'visible' });
    const testTime = Date.now() - testStart;

    // 测试连接时间应该小于 5 秒
    expect(testTime).toBeLessThan(5000);

    // 3. 测试搜索响应时间
    const searchStart = Date.now();
    await page.fill('input[placeholder="搜索用户名或邮箱..."]', 'test');
    await page.click('text=搜索');
    await page.waitForSelector('text=emma_bean'); // 假设搜索结果
    const searchTime = Date.now() - searchStart;

    // 搜索响应时间应该小于 2 秒
    expect(searchTime).toBeLessThan(2000);
  });
});