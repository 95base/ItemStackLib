/*
 * Written in 2018 by Daniel Saukel
 *
 * To the extent possible under law, the author(s) have dedicated all
 * copyright and related and neighboring rights to this software
 * to the public domain worldwide.
 *
 * This software is distributed without any warranty.
 *
 * You should have received a copy of the CC0 Public Domain Dedication
 * along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package de.erethon.commons.item;

import de.erethon.commons.compatibility.CompatibilityHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.inventory.ItemStack;

/**
 * @author Daniel Saukel
 */
class New extends InternalsProvider {

    /* META */
    public static String INTERNALS_VERSION = CompatibilityHandler.getInstance().getInternals().toString();
    public static String ORG_BUKKIT_CRAFTBUKKIT = "org.bukkit.craftbukkit." + INTERNALS_VERSION;
    public static String NET_MINECRAFT_SERVER = "net.minecraft.server." + INTERNALS_VERSION;

    /* ITEM STACK */
    public static Class ITEMSTACK;
    public static Method ITEMSTACK_GET_TAG;
    public static Method ITEMSTACK_SET_TAG;

    public static Class CRAFTITEMSTACK;
    public static Method CRAFTITEMSTACK_AS_BUKKIT_COPY;
    public static Method CRAFTITEMSTACK_AS_NMS_COPY;

    /* NBT */
    public static Class NBT_BASE;

    public static Class NBT_TAG_COMPOUND;
    public static Method NBT_TAG_COMPOUND_SET;

    public static Class NBT_TAG_LIST;
    public static Method NBT_TAG_LIST_ADD;

    public static Class NBT_TAG_STRING;
    public static Constructor NBT_TAG_STRING_CONSTRUCTOR;

    static {
        try {
            NBT_BASE = Class.forName(NET_MINECRAFT_SERVER + ".NBTBase");

            NBT_TAG_COMPOUND = Class.forName(NET_MINECRAFT_SERVER + ".NBTTagCompound");
            NBT_TAG_COMPOUND_SET = NBT_TAG_COMPOUND.getDeclaredMethod("set", String.class, NBT_BASE);

            NBT_TAG_LIST = Class.forName(NET_MINECRAFT_SERVER + ".NBTTagList");
            NBT_TAG_LIST_ADD = NBT_TAG_LIST.getDeclaredMethod("add", NBT_BASE);

            NBT_TAG_STRING = Class.forName(NET_MINECRAFT_SERVER + ".NBTTagString");
            NBT_TAG_STRING_CONSTRUCTOR = NBT_TAG_STRING.getConstructor(String.class);

            ITEMSTACK = Class.forName(NET_MINECRAFT_SERVER + ".ItemStack");
            ITEMSTACK_GET_TAG = ITEMSTACK.getDeclaredMethod("getTag");
            ITEMSTACK_SET_TAG = ITEMSTACK.getDeclaredMethod("setTag", NBT_TAG_COMPOUND);

            CRAFTITEMSTACK = Class.forName(ORG_BUKKIT_CRAFTBUKKIT + ".inventory.CraftItemStack");
            CRAFTITEMSTACK_AS_BUKKIT_COPY = CRAFTITEMSTACK.getDeclaredMethod("asBukkitCopy", ITEMSTACK);
            CRAFTITEMSTACK_AS_NMS_COPY = CRAFTITEMSTACK.getDeclaredMethod("asNMSCopy", ItemStack.class);

        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException exception) {
        }
    }

    @Override
    ItemStack setAttribute(ItemStack itemStack, String attributeName, double amount, byte operation, String... slots) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    ItemStack setSkullOwner(ItemStack itemStack, String id, String textureValue) {
        try {
            Object nmsStack = CRAFTITEMSTACK_AS_NMS_COPY.invoke(null, itemStack);

            Object compound = ITEMSTACK_GET_TAG.invoke(nmsStack);
            if (compound == null) {
                compound = NBT_TAG_COMPOUND.newInstance();
            }

            Object skullOwner = NBT_TAG_COMPOUND.newInstance();
            NBT_TAG_COMPOUND_SET.invoke(skullOwner, "Id", NBT_TAG_STRING_CONSTRUCTOR.newInstance(id));
            Object properties = NBT_TAG_COMPOUND.newInstance();
            Object textures = NBT_TAG_LIST.newInstance();
            Object value = NBT_TAG_COMPOUND.newInstance();
            NBT_TAG_COMPOUND_SET.invoke(value, "Value", NBT_TAG_STRING_CONSTRUCTOR.newInstance(textureValue));
            NBT_TAG_LIST_ADD.invoke(textures, value);
            NBT_TAG_COMPOUND_SET.invoke(properties, "textures", textures);
            NBT_TAG_COMPOUND_SET.invoke(skullOwner, "Properties", properties);

            NBT_TAG_COMPOUND_SET.invoke(compound, "SkullOwner", skullOwner);
            ITEMSTACK_SET_TAG.invoke(nmsStack, compound);
            return (ItemStack) CRAFTITEMSTACK_AS_BUKKIT_COPY.invoke(null, nmsStack);

        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
            return itemStack;
        }
    }

}