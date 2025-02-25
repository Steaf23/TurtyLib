package io.github.darealturtywurty.turtylib.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class GuiUtils {
    private GuiUtils() {
        throw new IllegalStateException("Attempted to construct utility class!");
    }

    public static void drawCenteredString(PoseStack stack, Component text, int xPos, int yPos, int colour) {
        final int textWidth = ClientUtils.getFont().width(text);
        ClientUtils.getFont().draw(stack, text, xPos - textWidth / 2, yPos, colour);
    }
    
    public static void drawDebugOutline(PoseStack stack, int xPos, int yPos, int width, int height, int thickness,
        int argbColor) {
        final FourVec2 top = MathUtils.getFourVec(stack, xPos, yPos, xPos + width, yPos, thickness);
        final FourVec2 bottom = MathUtils.getFourVec(stack, xPos, yPos + height, xPos + width, yPos + height,
            thickness);
        final FourVec2 left = MathUtils.getFourVec(stack, xPos, yPos, xPos, yPos + height, thickness);
        final FourVec2 right = MathUtils.getFourVec(stack, xPos + width, yPos, xPos + width, yPos + height, thickness);
        
        GuiUtils.drawLine(stack, top.first(), top.second(), top.third(), top.fourth(), argbColor);
        GuiUtils.drawLine(stack, bottom.first(), bottom.second(), bottom.third(), bottom.fourth(), argbColor);
        GuiUtils.drawLine(stack, left.first(), left.second(), left.third(), left.fourth(), argbColor);
        GuiUtils.drawLine(stack, right.first(), right.second(), right.third(), right.fourth(), argbColor);
    }
    
    public static void drawLine(double startX, double startY, double endX, double endY, int red, int green, int blue,
        int alpha, float lineWidth) {
        RenderSystem.assertOnRenderThread();
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        final var tesselator = RenderSystem.renderThreadTesselator();
        final BufferBuilder buffer = tesselator.getBuilder();
        RenderSystem.lineWidth(lineWidth);
        buffer.begin(Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        buffer.vertex(startX, startY, 0).color(red, green, blue, alpha).normal(1.0F, 0.0F, 0.0F).endVertex();
        buffer.vertex(endX, endY, 0).color(red, green, blue, alpha).normal(1.0F, 0.0F, 0.0F).endVertex();
        tesselator.end();
        RenderSystem.lineWidth(1.0F);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
    }

    public static void drawLine(PoseStack stack, Vec2 start0, Vec2 start1, Vec2 end0, Vec2 end1, int argbColor) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        final BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(stack.last().pose(), start0.x, start0.y, 0).color(argbColor).endVertex();
        buffer.vertex(stack.last().pose(), end1.x, end1.y, 0).color(argbColor).endVertex();
        buffer.vertex(stack.last().pose(), end0.x, end0.y, 0).color(argbColor).endVertex();
        buffer.vertex(stack.last().pose(), start1.x, start1.y, 0).color(argbColor).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }
    
    public static void drawQuad(PoseStack stack, int x0, int y0, int x1, int y1, int argbColor) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        final BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(stack.last().pose(), x0, y1, 0).color(argbColor).endVertex();
        buffer.vertex(stack.last().pose(), x1, y1, 0).color(argbColor).endVertex();
        buffer.vertex(stack.last().pose(), x1, y0, 0).color(argbColor).endVertex();
        buffer.vertex(stack.last().pose(), x0, y0, 0).color(argbColor).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }

    public static void drawQuadSplitTexture(PoseStack stack, int x, int y, int width, int height, int imageWidth,
        int imageHeight) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, Resources.TAB_BACKGROUND);
        GuiComponent.blit(stack, x, y, 0, 0, width / 2, height / 2, imageWidth, imageHeight);
        GuiComponent.blit(stack, x + width / 2, y, imageWidth - width / 2, 0, width / 2, height / 2, imageWidth,
            imageHeight);
        GuiComponent.blit(stack, x, y + height / 2, 0, imageHeight - height / 2, width / 2, height / 2, imageWidth,
            imageHeight);
        GuiComponent.blit(stack, x + width / 2, y + height / 2, imageWidth - width / 2, imageHeight - height / 2,
            width / 2, height / 2, imageWidth, imageHeight);
    }

    public static void drawWordWrap(PoseStack stack, FormattedText text, float x, float y, int length, int color) {
        final Matrix4f matrix4f = stack.last().pose();
        
        for (final FormattedCharSequence line : ClientUtils.getFont().split(text, length)) {
            ClientUtils.getFont().drawInternal(line, x, y, color, matrix4f, false);
            y += 9;
        }
    }

    public static void renderEntity(PoseStack stack, Entity entity, Vec3 rotation, Vec3 scale, Vec3 offset, int xPos,
        int yPos, float partialTicks) {
        stack.pushPose();
        stack.translate(xPos, yPos, 1050.0F);
        stack.scale(1.0F, 1.0F, -1.0F);
        stack.translate(0.0D, 0.0D, 1000.0D);
        stack.scale((float) scale.x(), (float) scale.y(), (float) scale.z());
        final Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        stack.mulPose(quaternion);
        stack.translate(offset.x(), offset.y(), offset.z());
        stack.mulPose(new Quaternion((float) -rotation.x(), (float) -rotation.y(), (float) -rotation.z(), true));
        final EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        final MultiBufferSource.BufferSource buffer = MultiBufferSource
            .immediate(Tesselator.getInstance().getBuilder());
        render(renderManager, entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, stack, buffer, 15728880);
        buffer.endBatch();
        stack.popPose();
    }
    
    public static void renderLoopSprite(TextureAtlasSprite sprite, PoseStack stack, int x0, int y0, int x1, int y1,
        int loopX, int loopY) {
        final int width = x1 - x0, height = y1 - y0;
        final int leftoverX = width % loopX, leftoverY = height % loopY;
        
        for (int x = 0; x < Math.floor(width / (float) loopX); x++) {
            for (int y = 0; y < Math.floor(height / (float) loopY); y++) {
                renderSprite(sprite, stack, x0 + x * loopX, y0 + y * loopY, x0 + x * loopX + loopX,
                    y0 + y * loopY + loopY);
            }
        }

        final float relWidth = sprite.getU1() - sprite.getU0();
        final float resultX = leftoverX / (float) loopX * relWidth + sprite.getU0();
        final float relHeight = sprite.getV1() - sprite.getV0();
        final float resultY = leftoverY / (float) loopY * relHeight + sprite.getV0();
        
        if (leftoverX > 0) {
            for (int x = 0; x < Math.floor(width / (float) loopX); x++) {
                renderSprite(sprite, stack, x0 + x * loopX, y1 - leftoverY, x0 + x * loopX + loopX, y1, sprite.getU0(),
                    sprite.getV0(), sprite.getU1(), resultY);
            }
        }
        
        if (leftoverY > 0) {
            for (int y = 0; y < Math.floor(height / (float) loopY); y++) {
                renderSprite(sprite, stack, x1 - leftoverX, y0 + y * loopY, x1, y0 + y * loopY + loopY, sprite.getU0(),
                    sprite.getV0(), resultX, sprite.getV1());
            }
        }
        
        renderSprite(sprite, stack, x0 + width - leftoverX, y0 + height - leftoverY, x1, y1, sprite.getU0(),
            sprite.getV0(), resultX, resultY);
    }

    public static void renderSprite(TextureAtlasSprite sprite, PoseStack stack, int x0, int y0, int x1, int y1) {
        renderSprite(sprite, stack, x0, y0, x1, y1, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
    }

    public static void renderSprite(TextureAtlasSprite sprite, PoseStack stack, int x0, int y0, int x1, int y1,
        float u0, float v0, float u1, float v1) {
        final BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(stack.last().pose(), x0, y1, 0).uv(u0, v1).endVertex();
        buffer.vertex(stack.last().pose(), x1, y1, 0).uv(u1, v1).endVertex();
        buffer.vertex(stack.last().pose(), x1, y0, 0).uv(u1, v0).endVertex();
        buffer.vertex(stack.last().pose(), x0, y0, 0).uv(u0, v0).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }

    @SuppressWarnings("resource")
    private static <E extends Entity> void render(EntityRenderDispatcher renderManager, E entity, double xPos,
        double yPos, double zPos, float rotation, float delta, PoseStack poseStack, MultiBufferSource buffer,
        int packedLight) {
        final EntityRenderer<? super E> entityRenderer = renderManager.getRenderer(entity);
        
        try {
            final Vec3 renderOffset = entityRenderer.getRenderOffset(entity, delta);
            final double finalX = xPos + renderOffset.x();
            final double finalY = yPos + renderOffset.y();
            final double finalZ = zPos + renderOffset.z();
            poseStack.pushPose();
            poseStack.translate(finalX, finalY, finalZ);
            entityRenderer.render(entity, rotation, delta, poseStack, buffer, packedLight);
            
            if (entity.displayFireAnimation()) {
                renderManager.renderFlame(poseStack, buffer, entity);
            }
            
            poseStack.translate(-renderOffset.x(), -renderOffset.y(), -renderOffset.z());
            poseStack.popPose();
        } catch (final Exception exception) {
            final CrashReport crashReport = CrashReport.forThrowable(exception, "Rendering entity in world");
            final CrashReportCategory entityCategory = crashReport.addCategory("Entity being rendered");
            entity.fillCrashReportCategory(entityCategory);
            final CrashReportCategory detailsCategory = crashReport.addCategory("Renderer details");
            detailsCategory.setDetail("Assigned renderer", entityRenderer);
            detailsCategory.setDetail("Location",
                CrashReportCategory.formatLocation(Minecraft.getInstance().level, xPos, yPos, zPos));
            detailsCategory.setDetail("Rotation", rotation);
            detailsCategory.setDetail("Delta", delta);
            throw new ReportedException(crashReport);
        }
    }
}
