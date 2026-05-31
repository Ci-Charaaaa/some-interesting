package com.chara.some_interesting.component;

import com.chara.some_interesting.SomeInteresting;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class ModComponent {

    //给启动类调用的初始化方法
    protected static void initialize(){}

    //自定义一个储存整数的留空数据组件
    public static final DataComponentType<Integer> RESERVE_COMPONENT_TYPE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(SomeInteresting.MOD_ID, "Reserve_proficiency_component"),
            DataComponentType.<Integer>builder().persistent(null).build()
    );


}
