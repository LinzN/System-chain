/*
 * Copyright (c) 2025 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */


package de.linzn.systemChain.callbacks;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.stem.modules.informationModule.InformationBlock;
import de.linzn.stem.modules.informationModule.InformationIntent;
import de.linzn.stem.taskManagment.AbstractCallback;
import de.linzn.stem.taskManagment.CallbackTime;
import de.linzn.stem.taskManagment.operations.OperationOutput;
import de.linzn.stem.taskManagment.operations.defaultOperations.ShellOperation;
import de.linzn.systemChain.SystemChainPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TemperatureScheduler extends AbstractCallback {

    private static volatile double hottestCore = 0;
    private final FileConfiguration fileConfiguration;
    private final int[] heat = {95, 98, 100};
    private int last = 0;
    private InformationBlock oldInformationBlock;

    public TemperatureScheduler() {
        fileConfiguration = YamlConfiguration.loadConfiguration(new File(SystemChainPlugin.systemChainPlugin.getDataFolder(), "temperatureScheduler.yml"));
        fileConfiguration.get("hostname", "test");
        fileConfiguration.get("username", "test");
        fileConfiguration.get("port", 22);
        fileConfiguration.get("command", "ssh test");
        fileConfiguration.save();
        this.oldInformationBlock = null;
    }

    public static double getHottestCore() {
        return hottestCore;
    }

    private static Float parseTemperature(String line) {
        String number = line == null ? "" : line.replaceAll("[^\\d.]", "");
        if (number.isEmpty()) {
            return null;
        }
        try {
            return Float.parseFloat(number);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void operation() {
        ShellOperation shellOperation = new ShellOperation();
        shellOperation.setUseSSH(true);
        shellOperation.setSshUser(fileConfiguration.getString("username"));
        shellOperation.setSshHost(fileConfiguration.getString("hostname"));
        shellOperation.setSshPort(fileConfiguration.getInt("port"));
        shellOperation.setScriptCommand(fileConfiguration.getString("command"));
        shellOperation.setUseOutput(true);
        addOperationData(shellOperation);
    }

    @Override
    public synchronized void callback(OperationOutput operationOutput) {
        if (operationOutput.getExit() != 0 || !(operationOutput.getData() instanceof List<?> list)) {
            SystemChainPlugin.systemChainPlugin.getPluginLogger().debug("Temperature query failed with exit " + operationOutput.getExit());
            return;
        }
        List<Float> floatList = new ArrayList<>();
        for (Object line : list) {
            Float value = parseTemperature(String.valueOf(line));
            if (value != null) {
                floatList.add(value);
            }
        }
        if (floatList.isEmpty()) {
            return;
        }

        double hotCore = -1;
        for (Float value : floatList) {
            hotCore = Math.max(hotCore, value);
        }
        hottestCore = hotCore;

        InformationBlock informationBlock = null;
        SystemChainPlugin plugin = SystemChainPlugin.systemChainPlugin;
        if (hotCore >= heat[2]) {
            if (last < 3) {
                last = 3;
                informationBlock = new InformationBlock("System-Temperature", "Die Temperatur des Hostsystems liegt mit " + hotCore + "°C im kritischen Bereich!", plugin);
                informationBlock.setExpireTime(-1);
            }
        } else if (hotCore >= heat[1]) {
            if (last < 2) {
                last = 2;
                informationBlock = new InformationBlock("System-Temperature", "Die Temperatur des Prozessors ist gefährlich heiß. " + hotCore + "°C", plugin);
                informationBlock.setExpireTime(-1);
            }
        } else if (hotCore >= heat[0]) {
            if (last < 1) {
                last = 1;
                informationBlock = new InformationBlock("System-Temperature", "Der Prozessor des Hostsystems ist mit " + hotCore + "°C ungewöhnlich heiß.", plugin);
                informationBlock.setExpireTime(-1);
            }
        } else if (last > 0) {
            last = 0;
            informationBlock = new InformationBlock("System-Temperature", "Die Temperatur des Prozessors ist wieder normal. " + hotCore + "°C", plugin);
            informationBlock.setExpireTime(0);
        }

        if (informationBlock != null) {
            if (oldInformationBlock != null) {
                oldInformationBlock.expire();
            }
            oldInformationBlock = informationBlock;
            informationBlock.setIcon("FIRE");
            informationBlock.addIntent(InformationIntent.SHOW_DISPLAY);
            informationBlock.addIntent(InformationIntent.NOTIFY_USER);
            plugin.getStemApi().information().queue(informationBlock);
        }
        plugin.getPluginLogger().debug("Core temperatures " + floatList);
    }

    @Override
    public CallbackTime getTime() {
        return new CallbackTime(10, 20, TimeUnit.SECONDS);
    }
}
