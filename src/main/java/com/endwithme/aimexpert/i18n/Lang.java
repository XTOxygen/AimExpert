package com.endwithme.aimexpert.i18n;

import java.util.HashMap;
import java.util.Map;

public final class Lang
{
    public static final String EN_US = "en_US";
    public static final String ZH_CN = "zh_CN";

    private static final Map<String, String> EN = new HashMap<String, String>();
    private static final Map<String, String> ZH = new HashMap<String, String>();

    private static String language = EN_US;

    private Lang()
    {
    }

    public static void setLanguage(String code)
    {
        language = ZH_CN.equals(code) ? ZH_CN : EN_US;
    }

    public static String getLanguage()
    {
        return language;
    }

    public static boolean isChinese()
    {
        return ZH_CN.equals(language);
    }

    public static String get(String key)
    {
        if (key == null)
        {
            return "";
        }

        Map<String, String> map = isChinese() ? ZH : EN;
        String value = map.get(key);
        if (value == null)
        {
            value = EN.get(key);
        }
        return value == null ? key : value;
    }

    public static String get(String key, Object... args)
    {
        return String.format(get(key), args);
    }

    private static void put(String key, String en, String zh)
    {
        EN.put(key, en);
        ZH.put(key, zh);
    }

    static
    {
        put("gui.lang", "Language", "语言");
        put("gui.hint.close", "Press ESC to close, scroll the mouse wheel to browse", "按 ESC 关闭，滚轮浏览设置");

        put("card.general", "General", "通用");
        put("card.crosshair", "Crosshair", "准星");
        put("card.dynamic", "Dynamic", "动态");
        put("card.template", "Template", "模板");
        put("card.draw", "Drawing board", "绘画板");
        put("card.share", "Share", "分享");

        put("draw.type", "Shape", "图案");
        put("draw.type.normal", "Nrm", "普通");
        put("draw.type.friendly", "Fr", "友好");
        put("draw.type.hostile", "Hos", "敌对");
        put("draw.type.fov", "FOV", "视角");
        put("draw.type.attack", "Atk", "攻击");
        put("draw.size", "Grid size", "网格尺寸");
        put("draw.mirror", "Mirror drawing", "对称绘制");
        put("draw.hint", "Left click draws, right click erases", "左键绘制，右键擦除");
        put("draw.clear", "Clear", "清空");
        put("draw.invert", "Invert", "反色");
        put("draw.copy", "Copy shape", "复制图案");
        put("draw.paste", "Paste shape", "粘贴图案");
        put("draw.copied", "Shape copied", "图案已复制");
        put("draw.nothing", "Nothing to paste yet", "还没有可粘贴的图案");
        put("draw.empty", "Draw some dots first", "请先绘制图案");
        put("draw.name", "Template name", "模板名称");
        put("draw.save", "Save as template", "保存为模板");
        put("draw.saved", "Saved as template: %s", "已保存为模板：%s");

        put("general.blur", "Background blur", "背景模糊");
        put("general.blurStrength", "Blur strength", "模糊强度");
        put("general.animations", "Animations", "过渡动画");
        put("general.keyHint", "Open GUI: %s (Controls menu)", "打开界面：%s（按键设置）");
        put("general.about", "AimExpert v%s by %s", "AimExpert v%s 作者 %s");

        put("crosshair.useCustom", "Custom crosshair", "自定义准星");
        put("crosshair.hide", "Hide crosshair", "隐藏准星");
        put("crosshair.size", "Size", "大小");
        put("crosshair.opacity", "Opacity", "不透明度");
        put("crosshair.range", "Trigger range", "触发距离");
        put("crosshair.type.normal", "Normal", "普通");
        put("crosshair.type.friendly", "Friendly mob", "友好生物");
        put("crosshair.type.hostile", "Hostile mob", "敌对生物");
        put("crosshair.type.fov", "FOV movement", "视角移动");
        put("crosshair.type.attack", "Attacking", "攻击");
        put("crosshair.patternHint", "Shapes come from the active template", "图案来自当前模板");
        put("crosshair.players", "Other players", "其他玩家");
        put("crosshair.playersHostile", "Hostile", "敌对");
        put("crosshair.playersFriendly", "Friendly", "友好");
        put("crosshair.playersIgnore", "Ignore", "忽略");

        put("dynamic.enabled", "Dynamic crosshair", "动态准星");
        put("dynamic.fov", "React to FOV movement", "视角移动时变化");
        put("dynamic.attack", "React to attacking", "攻击时变化");
        put("dynamic.amount", "Spread amount", "扩散幅度");
        put("dynamic.speed", "Response speed", "响应速度");
        put("dynamic.switch", "Switch animation", "切换动画");
        put("dynamic.duration", "Animation duration", "动画时长");
        put("dynamic.direction", "Direction", "方向");
        put("dynamic.smallToLarge", "Small to large", "由小变大");
        put("dynamic.largeToSmall", "Large to small", "由大变小");
        put("dynamic.status", "Now: %s  x%s", "当前：%s  x%s");

        put("template.name", "Template name", "模板名称");
        put("template.list", "Templates", "模板列表");
        put("template.load", "Load", "加载");
        put("template.save", "Save", "保存");
        put("template.delete", "Delete", "删除");
        put("template.local", "Local", "本地");
        put("template.export", "Copy JSON", "复制 JSON");
        put("template.import", "Paste JSON", "粘贴 JSON");
        put("template.reset", "Reset to Local", "恢复本地模板");
        put("template.saved", "Template saved: %s", "模板已保存：%s");
        put("template.loaded", "Template loaded: %s", "已加载模板：%s");
        put("template.deleted", "Template deleted: %s", "已删除模板：%s");
        put("template.invalid", "Invalid template JSON", "模板 JSON 无效");
        put("template.copied", "Template JSON copied to clipboard", "模板 JSON 已复制到剪贴板");
        put("template.copyFailed", "Could not access the clipboard", "无法访问剪贴板");
        put("template.clipboardEmpty", "The clipboard is empty", "剪贴板为空");
        put("template.imported", "Template imported: %s", "已导入模板：%s");
        put("template.emptyName", "Enter a template name first", "请先输入模板名称");
        put("template.notFound", "Template not found: %s", "未找到模板：%s");
        put("template.builtin", "The built-in template cannot be overwritten", "内置模板不可覆盖");
        put("template.deletedBuiltin", "The built-in template cannot be deleted", "内置模板不可删除");

        put("share.recipient", "Recipient", "接收者");
        put("share.send", "Share", "发送");
        put("share.sendAll", "Share to all", "发送给所有人");
        put("share.hint1", "Method 1: /shareaim <template> [player]", "方法一：/shareaim <模板> [玩家]");
        put("share.hint2", "Method 2: copy the JSON and paste it to others", "方法二：复制 JSON 后粘贴给对方");
        put("share.status", "Server relay: %s", "服务器转发：%s");
        put("share.statusReady", "ready", "可用");
        put("share.statusPending", "connecting...", "连接中...");
        put("share.statusUnsupported", "not available", "不可用");
        put("share.pending", "Pending offers: %s", "待处理分享：%s");
        put("share.accept", "Accept", "接受");
        put("share.decline", "Decline", "拒绝");
        put("share.acceptHover", "Click to load this crosshair template", "点击加载该准星模板");
        put("share.declineHover", "Click to decline this template", "点击拒绝该模板");
        put("share.offerText", " has shared a crosshair template with you. Accept?", " 向你分享了一个准星模板，是否接受？");
        put("share.unsupported", "This server has no AimExpert, use Copy JSON instead", "该服务器未安装 AimExpert，请改用复制 JSON");
        put("share.wait", "Talking to the server, please try again in a moment", "正在与服务器通信，请稍后再试");
        put("share.noRecipient", "No AimExpert user found on this server", "本服务器未找到使用 AimExpert 的玩家");
        put("share.sent", "You have shared the template %s with %s", "你已将模板 %s 分享给 %s");
        put("share.sentAll", "You have shared the template %s with %s player(s)", "你已将模板 %s 分享给 %s 名玩家");
        put("share.accepted", "Crosshair template accepted", "已接受准星模板");
        put("share.declined", "Crosshair template declined", "已拒绝准星模板");
        put("share.acceptedBy", "%s accepted your crosshair template %s", "%s 接受了你的准星模板 %s");
        put("share.declinedBy", "%s declined your crosshair template %s", "%s 拒绝了你的准星模板 %s");
        put("share.gone", "That offer is no longer available", "该分享已失效");

        put("command.usage", "Usage: /shareaim <template> [player]", "用法：/shareaim <模板> [玩家]");
        put("command.unknownTemplate", "Unknown template: %s", "未知模板：%s");
        put("command.noServer", "Join a world first", "请先进入世界");
        put("command.help", "/aimexpert <open|accept|decline|copy|paste|share> [value]", "/aimexpert <open|accept|decline|copy|paste|share> [值]");
    }
}
