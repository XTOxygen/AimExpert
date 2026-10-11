package com.endwithme.aimexpert.command;

import java.util.ArrayList;
import java.util.List;

import com.endwithme.aimexpert.AimExpert;
import com.endwithme.aimexpert.crosshair.TemplateManager;
import com.endwithme.aimexpert.i18n.Lang;
import com.endwithme.aimexpert.share.ShareManager;
import com.endwithme.aimexpert.util.ChatHelper;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Client side /aimexpert command, also used by the clickable chat buttons. */
@SideOnly(Side.CLIENT)
public class CommandAimExpert implements ICommand
{
    @Override
    public String getCommandName()
    {
        return "aimexpert";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/aimexpert <open|accept|decline|copy|paste|share> [value]";
    }

    @Override
    public List<String> getCommandAliases()
    {
        List<String> aliases = new ArrayList<String>();
        aliases.add("ae");
        return aliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            ChatHelper.send(Lang.get("command.help"));
            return;
        }

        String sub = args[0].toLowerCase();

        if ("open".equals(sub) || "gui".equals(sub))
        {
            AimExpert.proxy.openGui();
            return;
        }

        if ("accept".equals(sub) || "decline".equals(sub))
        {
            if (args.length < 2)
            {
                ChatHelper.send(Lang.get("command.help"));
                return;
            }

            int id;
            try
            {
                id = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e)
            {
                ChatHelper.send(Lang.get("share.gone"));
                return;
            }

            if ("accept".equals(sub))
            {
                ShareManager.accept(id);
            }
            else
            {
                ShareManager.decline(id);
            }
            return;
        }

        if ("copy".equals(sub))
        {
            String name = args.length > 1 ? args[1] : TemplateManager.getActiveName();
            ShareManager.copyJsonToClipboard(name);
            return;
        }

        if ("paste".equals(sub))
        {
            ShareManager.importJsonFromClipboard();
            return;
        }

        if ("share".equals(sub))
        {
            if (args.length < 2)
            {
                ChatHelper.send(Lang.get("command.usage"));
                return;
            }

            ShareManager.share(args[1], args.length > 2 ? args[2] : "");
            return;
        }

        ChatHelper.send(Lang.get("command.help"));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            List<String> options = new ArrayList<String>();
            options.add("open");
            options.add("accept");
            options.add("decline");
            options.add("copy");
            options.add("paste");
            options.add("share");
            return filter(args[0], options);
        }

        if (args.length == 2 && ("copy".equalsIgnoreCase(args[0]) || "share".equalsIgnoreCase(args[0])))
        {
            return filter(args[1], TemplateManager.getNames());
        }

        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 2;
    }

    @Override
    public int compareTo(ICommand other)
    {
        return this.getCommandName().compareTo(other.getCommandName());
    }

    private static List<String> filter(String prefix, List<String> options)
    {
        List<String> result = new ArrayList<String>();
        String lower = prefix == null ? "" : prefix.toLowerCase();
        for (String option : options)
        {
            if (option.toLowerCase().startsWith(lower))
            {
                result.add(option);
            }
        }
        return result;
    }
}
