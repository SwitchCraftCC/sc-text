package io.sc3.text.mixin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.sc3.text.TokenTextContent;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.reflect.Type;

@Mixin(Text.Serializer.class)
public class TextSerializerMixin {
  @Unique
  private final ThreadLocal<String> token = ThreadLocal.withInitial(() -> null);

  @Inject(
    method = "serialize(Lnet/minecraft/text/Text;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;",
    at = @At(
      value = "INVOKE",
      target = "Ljava/lang/IllegalArgumentException;<init>(Ljava/lang/String;)V"
    ),
    cancellable = true,
    locals = LocalCapture.CAPTURE_FAILHARD
  )
  private void serialize(
    Text text,
    Type type,
    JsonSerializationContext jsonSerializationContext,
    CallbackInfoReturnable<JsonElement> cir,
    JsonObject jsonObject
  ) {
    if (text.getContent() instanceof TokenTextContent tokenTextContent) {
      jsonObject.addProperty("text", "<token>");
      jsonObject.addProperty("token", tokenTextContent.getToken());
      cir.setReturnValue(jsonObject);
    }
  }

  @Inject(
    method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/text/MutableText;",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/util/JsonHelper;getString(Lcom/google/gson/JsonObject;Ljava/lang/String;)Ljava/lang/String;",
      ordinal = 0
    ),
    locals = LocalCapture.CAPTURE_FAILHARD
  )
  private void startDeserializingText(
    JsonElement jsonElement,
    Type type,
    JsonDeserializationContext jsonDeserializationContext,
    CallbackInfoReturnable<MutableText> cir,
    JsonObject jsonObject
  ) {
    if (jsonObject.has("token")) {
      String token = JsonHelper.getString(jsonObject, "token");
      this.token.set(token);
    }
  }

  @Redirect(
    method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/text/MutableText;",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/text/Text;literal(Ljava/lang/String;)Lnet/minecraft/text/MutableText;",
      ordinal = 1
    )
  )
  private MutableText applyTokenTextContent(String text) {
    if (token.get() != null) {
      return MutableText.of(new TokenTextContent(token.get()));
    } else {
      return MutableText.of(new LiteralTextContent(text));
    }
  }

  @Inject(
    method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/text/MutableText;",
    at = @At("HEAD")
  )
  private void clearLocalToken(CallbackInfoReturnable<MutableText> cir) {
    token.set(null);
  }
}
