package com.proofpoint.lifecycle;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.inject.matcher.Matchers.any;

/**
 * Guice module for binding the LifeCycle manager
 */
public class LifeCycleModule implements Module
{
    private final List<Object>                      injectedInstances = Lists.newArrayList();
    private final LifeCycleMethodsMap               lifeCycleMethodsMap = new LifeCycleMethodsMap();
    private final AtomicReference<LifeCycleManager> lifeCycleManagerRef = new AtomicReference<LifeCycleManager>(null);
    private final LifeCycleManager.Mode             mode;

    public LifeCycleModule()
    {
        this(LifeCycleManager.Mode.START_INSTANCES_IMMEDIATELY);
    }

    public LifeCycleModule(LifeCycleManager.Mode mode)
    {
        this.mode = mode;
    }

    @Override
    public void configure(Binder binder)
    {
        binder.bindListener(any(), new TypeListener()
        {
            @Override
            public <T> void hear(TypeLiteral<T> type, TypeEncounter<T> encounter)
            {
                encounter.register(new InjectionListener<T>()
                {
                    @Override
                    public void afterInjection(T obj)
                    {
                        if ( isLifeCycleClass(obj.getClass()) )
                        {
                            LifeCycleManager lifeCycleManager = lifeCycleManagerRef.get();
                            if ( lifeCycleManager != null )
                            {
                                try
                                {
                                    lifeCycleManager.addInstance(obj);
                                }
                                catch ( Exception e )
                                {
                                    throw new Error(e);
                                }
                            }
                            else
                            {
                                injectedInstances.add(obj);
                            }
                        }
                    }
                });
            }
        });
    }

    @Provides
    @Singleton
    public LifeCycleManager     getServerManager()
    {
        LifeCycleManager lifeCycleManager = new LifeCycleManager(injectedInstances, lifeCycleMethodsMap, mode);
        lifeCycleManagerRef.set(lifeCycleManager);
        return lifeCycleManager;
    }

    private boolean isLifeCycleClass(Class<?> clazz)
    {
        LifeCycleMethods        methods = lifeCycleMethodsMap.get(clazz);
        return methods.hasFor(PostConstruct.class) || methods.hasFor(PreDestroy.class);
    }
}
