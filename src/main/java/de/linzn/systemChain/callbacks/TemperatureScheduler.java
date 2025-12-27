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
import de.linzn.stem.STEMApp;
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

    private final FileConfiguration fileConfiguration;
    private final int[] heat = {95, 98, 100};
    private int last = 0;
    private static double hottestCore = 0;

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


    @Override
    public void operation() {
        String command = fileConfiguration.getString("command");
        String username = fileConfiguration.getString("username");
        String hostname = fileConfiguration.getString("hostname");
        int port = fileConfiguration.getInt("port");

        ShellOperation shellOperation = new ShellOperation();

        shellOperation.setUseSSH(true);
        shellOperation.setSshUser(username);
        shellOperation.setSshHost(hostname);
        shellOperation.setSshPort(port);

        shellOperation.setScriptCommand(command);
        shellOperation.setUseOutput(true);

        addOperationData(shellOperation);
    }

    @Override
    public void callback(OperationOutput operationOutput) {
        List<String> list = (List<String>) operationOutput.getData();
        ArrayList<Float> floatList = new ArrayList<>();
        for (String s : list) {
            floatList.add(getFloat(s));
        }
        double hotCore = -1;
        InformationBlock informationBlock = null;
        for (Float aFloatList : floatList) {
            double value = aFloatList;
            if (hotCore < value) {
                hotCore = value;
            }
        }
        hottestCore = hotCore;
        if (hotCore >= heat[0]) {

            if (hotCore >= heat[1]) {
                if (hotCore >= heat[2]) {
                    if (last < 3) {
                        last = 3;
                        informationBlock = new InformationBlock("System-Temperature", "Die Temperatur des Hostsystems liegt mit " + hotCore + "°C im kritischen Bereich!", SystemChainPlugin.systemChainPlugin);
                        informationBlock.setExpireTime(-1);
                    }
                } else {
                    if (last < 2) {
                        last = 2;
                        informationBlock = new InformationBlock("System-Temperature", "Die Temperatur des Prozessors ist gefährlich heiß. " + hotCore + "°C", SystemChainPlugin.systemChainPlugin);
                        informationBlock.setExpireTime(-1);
                    }
                }
            } else {
                if (last < 1) {
                    last = 1;
                    informationBlock = new InformationBlock("System-Temperature", "Der Prozessor des Hostsystems ist mit " + hotCore + "°C ungewöhnlich heiß.", SystemChainPlugin.systemChainPlugin);
                    informationBlock.setExpireTime(-1);
                }
            }
        } else {

            if (last > 0) {
                last = 0;
                informationBlock = new InformationBlock("System-Temperature", "Die Temperatur des Prozessors ist wieder normal. " + hotCore + "°C", SystemChainPlugin.systemChainPlugin);
                informationBlock.setExpireTime(0);
            }
        }
        if (informationBlock != null) {
            if (oldInformationBlock != null) {
                oldInformationBlock.expire();
            }
            oldInformationBlock = informationBlock;
            informationBlock.setIcon("FIRE");
            informationBlock.addIntent(InformationIntent.SHOW_DISPLAY);
            informationBlock.addIntent(InformationIntent.NOTIFY_USER);
            STEMApp.getInstance().getInformationModule().queueInformationBlock(informationBlock);
        }

        STEMApp.LOGGER.DEBUG("Core temperatures " + floatList);
    }

    @Override
    public CallbackTime getTime() {
        return new CallbackTime(10, 20, TimeUnit.SECONDS);
    }


    private float getFloat(String line) {
        return Float.valueOf(line.replaceAll("[^\\d.]", ""));
    }

}
