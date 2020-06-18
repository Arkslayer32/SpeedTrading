package adudecalledleo.speedtrading;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class SpeedTradingButton extends AbstractPressableButtonWidget {
    private final MerchantScreenAccess msa;
    private MerchantScreenAccess.TradeState cachedTradeState;
    private boolean trading;
    private boolean refill;

    public SpeedTradingButton(int x, int y, MerchantScreenAccess msa) {
        super(x, y, 18, 20, null);
        this.msa = msa;
        trading = false;
        refill = false;
        updateActiveState();
    }

    @Override
    public void onPress() {
        updateActiveState();
        if (active) {
            SpeedTradingAntiFreezeMeasure.reset();
            trading = true;
            refill = true;
        }
    }

    private void performTrade() {
        if (trading) {
            if (msa.isClosed()) {
                trading = false;
                return;
            }
            if (SpeedTradingAntiFreezeMeasure.doAction()) {
                if (refill)
                    msa.refillTradeSlots();
                else
                    msa.performTrade();
                refill = !refill;
                updateActiveState();
                trading = active;
                if (!trading) {
                    msa.clearTradeSlots();
                    updateActiveState();
                }
            }
        }
    }

    private static final Identifier BUTTON_LOCATION = new Identifier(SpeedTradingMod.MOD_ID, "textures/gui/speedtrade.png");

    public void updateActiveState() {
        active = (cachedTradeState = msa.getTradeState()) == MerchantScreenAccess.TradeState.CAN_PERFORM;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (msa.isClosed())
            return;
        performTrade();
        MinecraftClient client = MinecraftClient.getInstance();
        client.getTextureManager().bindTexture(BUTTON_LOCATION);
        RenderSystem.color4f(1, 1, 1, alpha);
        int i = getYImage(isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        drawTexture(matrices, x, y, getZOffset(), 0, i * 18, 20, 18, 54, 20);
        if (isHovered())
            renderToolTip(matrices, mouseX, mouseY);
    }

    @Override
    public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
        msa.renderTooltip(matrices, new TranslatableText("speedtrading.tooltip." + cachedTradeState.name().toLowerCase()),
                          mouseX, mouseY);
    }
}
