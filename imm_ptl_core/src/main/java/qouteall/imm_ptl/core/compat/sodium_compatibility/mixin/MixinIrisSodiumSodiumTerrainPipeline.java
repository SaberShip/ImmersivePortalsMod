package qouteall.imm_ptl.core.compat.sodium_compatibility.mixin;

import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.minecraft.client.gl.Program;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.render.ShaderCodeTransformation;

import java.util.Optional;

@Pseudo
@Mixin(value = SodiumTerrainPipeline.class, remap = false)
public class MixinIrisSodiumSodiumTerrainPipeline {
    @Inject(method = "getTerrainVertexShaderSource", at = @At("RETURN"), cancellable = true)
    private void onGetTerrainVertexShaderSource(CallbackInfoReturnable<Optional<String>> cir) {
        Optional<String> original = cir.getReturnValue();
        cir.setReturnValue(original.map(code ->
            ShaderCodeTransformation.transform(
                Program.Type.VERTEX,
                "iris_sodium_terrain_vertex",
                code
            )
        ));
    }
}
