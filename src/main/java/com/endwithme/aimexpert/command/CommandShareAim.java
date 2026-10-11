package com.endwithme.aimexpert.command;

import java.util.ArrayList;
import java.util.List;

import com.endwithme.aimexpert.crosshair.TemplateManager;
import com.endwithme.aimexpert.i18n.Lang;
import com.endwithme.aimexpert.share.ShareManager;
import com.endwithme.aimexpert.util.ChatHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Client side /shareaim command. */
@SideOnly(Side.CLIENT)
public class CommandShareAim implements ICommand
{
    @Override
    public String getCommandName()
    {
        return "shareaim";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/shareaim <template> [player]";
    }

    @Override
    public List<String> getCommandAliases()
    {
        return new ArrayList<String>();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            ChatHelper.send(Lang.get("command.usage"));
            return;
        }

        String template = args[0];
        String recipient = args.length > 1 ? args[1] : "";
        if (!TemplateManager.exists(template))
        {
            ChatHelper.send(Lang.get("command.unknownTemplate", template));
            return;
        }

        ShareManager.share(template, recipient);
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
            return filter(args[0], TemplateManager.getNames());
        }

        if (args.length == 2)
        {
            List<String> names = new ArrayList<String>();
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld != null)
            {
                for (EntityPlayer player : mc.theWorld.playerEntities)
                {
                    names.add(player.getName());
                }
            }
            return filter(args[1], names);
        }

        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 1;
    }

    @Override
    public int compareTo(ICommand other)
    {
        return this.getCommandName().compareTo(other.getCommandName());
    }

    private static List<String> filter(String prefix, List<String> options)
    {
        List<String> result = new ArrayList<String>();
        if (options == null)
        {
            return result;
        }

        String lower = prefix == null ? "" : prefix.toLowerCase();
        for (String option : options)
        {
            if (option != null && option.toLowerCase().startsWith(lower))
            {
                result.add(option);
            }
        }
        return result;
    }
}
