package net.jrdemiurge.skyarena.scheduler;

import net.jrdemiurge.skyarena.SkyArena;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = SkyArena.MOD_ID)
public class Scheduler {
    private static final List<SchedulerTask> tasks = new ArrayList<>();

    public static void schedule(Runnable task, int delay) {
        synchronized (tasks) {
            tasks.add(new SchedulerTask(delay, task));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            synchronized (tasks) {
                Iterator<SchedulerTask> iterator = tasks.iterator();
                while (iterator.hasNext()) {
                    SchedulerTask st = iterator.next();
                    int newTicks = st.getTicksRemaining() - 1;
                    st.setTicksRemaining(newTicks);
                    if (newTicks <= 0) {
                        st.getTask().run();
                        iterator.remove();
                    }
                }
            }
        }
    }
}
