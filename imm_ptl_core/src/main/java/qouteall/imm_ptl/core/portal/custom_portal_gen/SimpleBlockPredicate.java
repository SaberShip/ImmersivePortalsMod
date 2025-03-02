package qouteall.imm_ptl.core.portal.custom_portal_gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import qouteall.q_misc_util.MiscHelper;

import java.util.function.Predicate;

// it could be either specified by a block or a block tag
public class SimpleBlockPredicate implements Predicate<BlockState> {
    public static final SimpleBlockPredicate pass = new SimpleBlockPredicate();
    
    private final Tag<Block> tag;
    private final Block block;
    
    public SimpleBlockPredicate(Tag<Block> tag) {
        this.tag = tag;
        this.block = null;
    }
    
    public SimpleBlockPredicate(Block block) {
        this.block = block;
        this.tag = null;
    }
    
    private SimpleBlockPredicate() {
        this.tag = null;
        this.block = null;
    }
    
    @Override
    public boolean test(BlockState blockState) {
        if (tag != null) {
            return blockState.isIn(tag);
        }
        else {
            if (block != null) {
                return blockState.getBlock() == block;
            }
            else {
                return true;
            }
        }
    }
    
    private static DataResult<SimpleBlockPredicate> deserialize(String string) {
        MinecraftServer server = MiscHelper.getServer();
        
        if (server == null) {
            return DataResult.error(
                "[Immersive Portals] Simple block predicate should not be deserialized in client"
            );
        }
        
        if (string.equals("minecraft:air")) {
            // to make it work for both normal air, cave air and void air
            return DataResult.success(new AirPredicate());
        }
        
        TagManager tagManager = server.serverResourceManager.getRegistryTagManager();
        Identifier id = new Identifier(string);
        Tag<Block> blockTag = tagManager.getOrCreateTagGroup(Registry.BLOCK_KEY).getTag(id);
        
        if (blockTag != null) {
            return DataResult.success(new SimpleBlockPredicate(blockTag), Lifecycle.stable());
        }
        
        if (Registry.BLOCK.getIds().contains(id)) {
            Block block = Registry.BLOCK.get(id);
            return DataResult.success(new SimpleBlockPredicate(block), Lifecycle.stable());
        }
        
        return DataResult.error("Unknown block or block tag:" + string);
    }
    
    private static String serialize(SimpleBlockPredicate predicate) {
        MinecraftServer server = MiscHelper.getServer();
        
        if (server == null) {
            throw new RuntimeException(
                "Simple block predicate should not be serialized in client"
            );
        }
        
        if (predicate.block != null) {
            return Registry.BLOCK.getId(predicate.block).toString();
        }
        
        Tag<Block> tag = predicate.tag;
        
        TagManager tagManager = server.serverResourceManager.getRegistryTagManager();
        
        Identifier id = tagManager.getOrCreateTagGroup(Registry.BLOCK_KEY).getId(((Tag.Identified) tag));
        
        if (id == null) {
            throw new RuntimeException(
                "Cannot get id from tag " + tag
            );
        }
        
        return id.toString();
    }
    
    public static final Codec<SimpleBlockPredicate> codec =
        Codec.STRING.comapFlatMap(
            SimpleBlockPredicate::deserialize,
            SimpleBlockPredicate::serialize
        );
    
    public static class AirPredicate extends SimpleBlockPredicate {
        @Override
        public boolean test(BlockState blockState) {
            return blockState.isAir();
        }
    }
    
}
