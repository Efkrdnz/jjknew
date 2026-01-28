package net.mcreator.jjkstrongest.network;

import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.Capability;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.JjkStrongestMod;

import java.util.function.Supplier;

import com.ibm.icu.util.Output;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class JjkStrongestModVariables {
	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		JjkStrongestMod.addNetworkMessage(PlayerVariablesSyncMessage.class, PlayerVariablesSyncMessage::buffer, PlayerVariablesSyncMessage::new, PlayerVariablesSyncMessage::handler);
	}

	@SubscribeEvent
	public static void init(RegisterCapabilitiesEvent event) {
		event.register(PlayerVariables.class);
	}

	@Mod.EventBusSubscriber
	public static class EventBusVariableHandlers {
		@SubscribeEvent
		public static void onPlayerLoggedInSyncPlayerVariables(PlayerEvent.PlayerLoggedInEvent event) {
			if (!event.getEntity().level().isClientSide())
				((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables())).syncPlayerVariables(event.getEntity());
		}

		@SubscribeEvent
		public static void onPlayerRespawnedSyncPlayerVariables(PlayerEvent.PlayerRespawnEvent event) {
			if (!event.getEntity().level().isClientSide())
				((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables())).syncPlayerVariables(event.getEntity());
		}

		@SubscribeEvent
		public static void onPlayerChangedDimensionSyncPlayerVariables(PlayerEvent.PlayerChangedDimensionEvent event) {
			if (!event.getEntity().level().isClientSide())
				((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables())).syncPlayerVariables(event.getEntity());
		}

		@SubscribeEvent
		public static void clonePlayer(PlayerEvent.Clone event) {
			event.getOriginal().revive();
			PlayerVariables original = ((PlayerVariables) event.getOriginal().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
			PlayerVariables clone = ((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
			clone.sorcerer = original.sorcerer;
			clone.RTC_unlocked = original.RTC_unlocked;
			clone.gojo_blue_unlocked = original.gojo_blue_unlocked;
			clone.wcs_power = original.wcs_power;
			clone.Output = original.Output;
			clone.Output_Dismantle = original.Output_Dismantle;
			clone.Output_Cleave = original.Output_Cleave;
			clone.Output_Flame = original.Output_Flame;
			clone.Output_Blue = original.Output_Blue;
			clone.Output_Red = original.Output_Red;
			clone.Output_Purple = original.Output_Purple;
			clone.Output_Domain = original.Output_Domain;
			clone.Output_HollowPurple = original.Output_HollowPurple;
			clone.Output_WorldSlash = original.Output_WorldSlash;
			clone.vow_overtime = original.vow_overtime;
			clone.vow_recoil = original.vow_recoil;
			clone.vow_wcsact = original.vow_wcsact;
			clone.vow_dismantleimbue = original.vow_dismantleimbue;
			clone.vow_RTChealbrain = original.vow_RTChealbrain;
			clone.vow_trueform = original.vow_trueform;
			clone.vow_executioner = original.vow_executioner;
			clone.vow_adaptivecleave = original.vow_adaptivecleave;
			clone.vow_shreddingbarrage = original.vow_shreddingbarrage;
			clone.vow_bladeresonance = original.vow_bladeresonance;
			clone.vow_smolderingbuildup = original.vow_smolderingbuildup;
			clone.vow_concentratedinferno = original.vow_concentratedinferno;
			clone.vow_desperateflames = original.vow_desperateflames;
			clone.vow_pyroclasm = original.vow_pyroclasm;
			clone.vow_meditativeopening = original.vow_meditativeopening;
			clone.vow_emptyhandshrine = original.vow_emptyhandshrine;
			clone.vow_overwhelmingmalevolence = original.vow_overwhelmingmalevolence;
			clone.vow_shrinesupremacy = original.vow_shrinesupremacy;
			clone.vow_overextendedmalice = original.vow_overextendedmalice;
			clone.vow_distanceamplification = original.vow_distanceamplification;
			clone.vow_stationaryperfection = original.vow_stationaryperfection;
			clone.vow_calculatedwarp = original.vow_calculatedwarp;
			clone.vow_overworlddominance = original.vow_overworlddominance;
			clone.vow_unencumberedfocus = original.vow_unencumberedfocus;
			clone.vow_pressuredexcellence = original.vow_pressuredexcellence;
			clone.vow_fairweatherfighter = original.vow_fairweatherfighter;
			clone.current_arm_animation = original.current_arm_animation;
			clone.arm_anim_holding = original.arm_anim_holding;
			clone.arm_anim_playing = original.arm_anim_playing;
			clone.arm_anim_progress = original.arm_anim_progress;
			clone.arm_anim_start_tick = original.arm_anim_start_tick;
			clone.marked_entities = original.marked_entities;
			clone.marked_timestamps = original.marked_timestamps;
			clone.blue_fist_toggle = original.blue_fist_toggle;
			clone.cleave_melee_toggle = original.cleave_melee_toggle;
			if (!event.isWasDeath()) {
				clone.wcs_x1 = original.wcs_x1;
				clone.wcs_y1 = original.wcs_y1;
				clone.wcs_z1 = original.wcs_z1;
				clone.wcs_x2 = original.wcs_x2;
				clone.wcs_y2 = original.wcs_y2;
				clone.wcs_z2 = original.wcs_z2;
				clone.charge_red = original.charge_red;
				clone.charge_blue = original.charge_blue;
				clone.charge_purple = original.charge_purple;
				clone.current_moveset = original.current_moveset;
				clone.dismantle_barrage = original.dismantle_barrage;
				clone.red_flight = original.red_flight;
				clone.infinity_crush = original.infinity_crush;
				clone.arm_translate_x = original.arm_translate_x;
				clone.arm_translate_y = original.arm_translate_y;
				clone.arm_translate_z = original.arm_translate_z;
				clone.arm_rotate_x = original.arm_rotate_x;
				clone.arm_rotate_y = original.arm_rotate_y;
				clone.arm_rotate_z = original.arm_rotate_z;
				clone.using_red = original.using_red;
				clone.domain_image_1 = original.domain_image_1;
				clone.domain_image_2 = original.domain_image_2;
				clone.wcs_chant_progress = original.wcs_chant_progress;
			}
		}
	}

	public static final Capability<PlayerVariables> PLAYER_VARIABLES_CAPABILITY = CapabilityManager.get(new CapabilityToken<PlayerVariables>() {
	});

	@Mod.EventBusSubscriber
	private static class PlayerVariablesProvider implements ICapabilitySerializable<Tag> {
		@SubscribeEvent
		public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
			if (event.getObject() instanceof Player && !(event.getObject() instanceof FakePlayer))
				event.addCapability(new ResourceLocation("jjk_strongest", "player_variables"), new PlayerVariablesProvider());
		}

		private final PlayerVariables playerVariables = new PlayerVariables();
		private final LazyOptional<PlayerVariables> instance = LazyOptional.of(() -> playerVariables);

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return cap == PLAYER_VARIABLES_CAPABILITY ? instance.cast() : LazyOptional.empty();
		}

		@Override
		public Tag serializeNBT() {
			return playerVariables.writeNBT();
		}

		@Override
		public void deserializeNBT(Tag nbt) {
			playerVariables.readNBT(nbt);
		}
	}

	public static class PlayerVariables {
		public String sorcerer = "\"\"";
		public double wcs_x1 = 0;
		public double wcs_y1 = 0;
		public double wcs_z1 = 0;
		public double wcs_x2 = 0;
		public double wcs_y2 = 0;
		public double wcs_z2 = 0;
		public double charge_red = 0;
		public double charge_blue = 0;
		public double charge_purple = 0;
		public String current_moveset = "\"\"";
		public boolean RTC_unlocked = false;
		public boolean gojo_blue_unlocked = true;
		public double wcs_power = 0;
		public boolean dismantle_barrage = false;
		public boolean red_flight = false;
		public double Output = 1.0;
		public double Output_Dismantle = 1.0;
		public double Output_Cleave = 1.0;
		public double Output_Flame = 1.0;
		public double Output_Blue = 1.0;
		public double Output_Red = 1.0;
		public double Output_Purple = 1.0;
		public double Output_Domain = 1.0;
		public double Output_HollowPurple = 1.0;
		public double Output_WorldSlash = 1.0;
		public boolean vow_overtime = false;
		public boolean vow_recoil = false;
		public boolean vow_wcsact = false;
		public boolean vow_dismantleimbue = false;
		public boolean vow_RTChealbrain = false;
		public boolean vow_trueform = false;
		public boolean vow_executioner = false;
		public boolean vow_adaptivecleave = false;
		public boolean vow_shreddingbarrage = false;
		public boolean vow_bladeresonance = false;
		public boolean vow_smolderingbuildup = false;
		public boolean vow_concentratedinferno = false;
		public boolean vow_desperateflames = false;
		public boolean vow_pyroclasm = false;
		public boolean vow_meditativeopening = false;
		public boolean vow_emptyhandshrine = false;
		public boolean vow_overwhelmingmalevolence = false;
		public boolean vow_shrinesupremacy = false;
		public boolean vow_overextendedmalice = false;
		public boolean vow_distanceamplification = false;
		public boolean vow_stationaryperfection = false;
		public boolean vow_calculatedwarp = false;
		public boolean vow_overworlddominance = false;
		public boolean vow_unencumberedfocus = false;
		public boolean vow_pressuredexcellence = false;
		public boolean vow_fairweatherfighter = false;
		public boolean infinity_crush = false;
		public double arm_translate_x = 0;
		public double arm_translate_y = 0;
		public double arm_translate_z = 0;
		public double arm_rotate_x = 0;
		public double arm_rotate_y = 0;
		public double arm_rotate_z = 0;
		public String current_arm_animation = "\"\"";
		public double arm_anim_holding = 0;
		public boolean arm_anim_playing = false;
		public double arm_anim_progress = 0;
		public double arm_anim_start_tick = 0;
		public boolean using_red = false;
		public String marked_entities = "\"\"";
		public String marked_timestamps = "\"\"";
		public boolean blue_fist_toggle = false;
		public boolean cleave_melee_toggle = false;
		public double domain_image_1 = 0;
		public double domain_image_2 = 0;
		public double wcs_chant_progress = 0;

		public void syncPlayerVariables(Entity entity) {
			if (entity instanceof ServerPlayer serverPlayer)
				JjkStrongestMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new PlayerVariablesSyncMessage(this));
		}

		public Tag writeNBT() {
			CompoundTag nbt = new CompoundTag();
			nbt.putString("sorcerer", sorcerer);
			nbt.putDouble("wcs_x1", wcs_x1);
			nbt.putDouble("wcs_y1", wcs_y1);
			nbt.putDouble("wcs_z1", wcs_z1);
			nbt.putDouble("wcs_x2", wcs_x2);
			nbt.putDouble("wcs_y2", wcs_y2);
			nbt.putDouble("wcs_z2", wcs_z2);
			nbt.putDouble("charge_red", charge_red);
			nbt.putDouble("charge_blue", charge_blue);
			nbt.putDouble("charge_purple", charge_purple);
			nbt.putString("current_moveset", current_moveset);
			nbt.putBoolean("RTC_unlocked", RTC_unlocked);
			nbt.putBoolean("gojo_blue_unlocked", gojo_blue_unlocked);
			nbt.putDouble("wcs_power", wcs_power);
			nbt.putBoolean("dismantle_barrage", dismantle_barrage);
			nbt.putBoolean("red_flight", red_flight);
			nbt.putDouble("Output", Output);
			nbt.putDouble("Output_Dismantle", Output_Dismantle);
			nbt.putDouble("Output_Cleave", Output_Cleave);
			nbt.putDouble("Output_Flame", Output_Flame);
			nbt.putDouble("Output_Blue", Output_Blue);
			nbt.putDouble("Output_Red", Output_Red);
			nbt.putDouble("Output_Purple", Output_Purple);
			nbt.putDouble("Output_Domain", Output_Domain);
			nbt.putDouble("Output_HollowPurple", Output_HollowPurple);
			nbt.putDouble("Output_WorldSlash", Output_WorldSlash);
			nbt.putBoolean("vow_overtime", vow_overtime);
			nbt.putBoolean("vow_recoil", vow_recoil);
			nbt.putBoolean("vow_wcsact", vow_wcsact);
			nbt.putBoolean("vow_dismantleimbue", vow_dismantleimbue);
			nbt.putBoolean("vow_RTChealbrain", vow_RTChealbrain);
			nbt.putBoolean("vow_trueform", vow_trueform);
			nbt.putBoolean("vow_executioner", vow_executioner);
			nbt.putBoolean("vow_adaptivecleave", vow_adaptivecleave);
			nbt.putBoolean("vow_shreddingbarrage", vow_shreddingbarrage);
			nbt.putBoolean("vow_bladeresonance", vow_bladeresonance);
			nbt.putBoolean("vow_smolderingbuildup", vow_smolderingbuildup);
			nbt.putBoolean("vow_concentratedinferno", vow_concentratedinferno);
			nbt.putBoolean("vow_desperateflames", vow_desperateflames);
			nbt.putBoolean("vow_pyroclasm", vow_pyroclasm);
			nbt.putBoolean("vow_meditativeopening", vow_meditativeopening);
			nbt.putBoolean("vow_emptyhandshrine", vow_emptyhandshrine);
			nbt.putBoolean("vow_overwhelmingmalevolence", vow_overwhelmingmalevolence);
			nbt.putBoolean("vow_shrinesupremacy", vow_shrinesupremacy);
			nbt.putBoolean("vow_overextendedmalice", vow_overextendedmalice);
			nbt.putBoolean("vow_distanceamplification", vow_distanceamplification);
			nbt.putBoolean("vow_stationaryperfection", vow_stationaryperfection);
			nbt.putBoolean("vow_calculatedwarp", vow_calculatedwarp);
			nbt.putBoolean("vow_overworlddominance", vow_overworlddominance);
			nbt.putBoolean("vow_unencumberedfocus", vow_unencumberedfocus);
			nbt.putBoolean("vow_pressuredexcellence", vow_pressuredexcellence);
			nbt.putBoolean("vow_fairweatherfighter", vow_fairweatherfighter);
			nbt.putBoolean("infinity_crush", infinity_crush);
			nbt.putDouble("arm_translate_x", arm_translate_x);
			nbt.putDouble("arm_translate_y", arm_translate_y);
			nbt.putDouble("arm_translate_z", arm_translate_z);
			nbt.putDouble("arm_rotate_x", arm_rotate_x);
			nbt.putDouble("arm_rotate_y", arm_rotate_y);
			nbt.putDouble("arm_rotate_z", arm_rotate_z);
			nbt.putString("current_arm_animation", current_arm_animation);
			nbt.putDouble("arm_anim_holding", arm_anim_holding);
			nbt.putBoolean("arm_anim_playing", arm_anim_playing);
			nbt.putDouble("arm_anim_progress", arm_anim_progress);
			nbt.putDouble("arm_anim_start_tick", arm_anim_start_tick);
			nbt.putBoolean("using_red", using_red);
			nbt.putString("marked_entities", marked_entities);
			nbt.putString("marked_timestamps", marked_timestamps);
			nbt.putBoolean("blue_fist_toggle", blue_fist_toggle);
			nbt.putBoolean("cleave_melee_toggle", cleave_melee_toggle);
			nbt.putDouble("domain_image_1", domain_image_1);
			nbt.putDouble("domain_image_2", domain_image_2);
			nbt.putDouble("wcs_chant_progress", wcs_chant_progress);
			return nbt;
		}

		public void readNBT(Tag tag) {
			CompoundTag nbt = (CompoundTag) tag;
			sorcerer = nbt.getString("sorcerer");
			wcs_x1 = nbt.getDouble("wcs_x1");
			wcs_y1 = nbt.getDouble("wcs_y1");
			wcs_z1 = nbt.getDouble("wcs_z1");
			wcs_x2 = nbt.getDouble("wcs_x2");
			wcs_y2 = nbt.getDouble("wcs_y2");
			wcs_z2 = nbt.getDouble("wcs_z2");
			charge_red = nbt.getDouble("charge_red");
			charge_blue = nbt.getDouble("charge_blue");
			charge_purple = nbt.getDouble("charge_purple");
			current_moveset = nbt.getString("current_moveset");
			RTC_unlocked = nbt.getBoolean("RTC_unlocked");
			gojo_blue_unlocked = nbt.getBoolean("gojo_blue_unlocked");
			wcs_power = nbt.getDouble("wcs_power");
			dismantle_barrage = nbt.getBoolean("dismantle_barrage");
			red_flight = nbt.getBoolean("red_flight");
			Output = nbt.getDouble("Output");
			Output_Dismantle = nbt.getDouble("Output_Dismantle");
			Output_Cleave = nbt.getDouble("Output_Cleave");
			Output_Flame = nbt.getDouble("Output_Flame");
			Output_Blue = nbt.getDouble("Output_Blue");
			Output_Red = nbt.getDouble("Output_Red");
			Output_Purple = nbt.getDouble("Output_Purple");
			Output_Domain = nbt.getDouble("Output_Domain");
			Output_HollowPurple = nbt.getDouble("Output_HollowPurple");
			Output_WorldSlash = nbt.getDouble("Output_WorldSlash");
			vow_overtime = nbt.getBoolean("vow_overtime");
			vow_recoil = nbt.getBoolean("vow_recoil");
			vow_wcsact = nbt.getBoolean("vow_wcsact");
			vow_dismantleimbue = nbt.getBoolean("vow_dismantleimbue");
			vow_RTChealbrain = nbt.getBoolean("vow_RTChealbrain");
			vow_trueform = nbt.getBoolean("vow_trueform");
			vow_executioner = nbt.getBoolean("vow_executioner");
			vow_adaptivecleave = nbt.getBoolean("vow_adaptivecleave");
			vow_shreddingbarrage = nbt.getBoolean("vow_shreddingbarrage");
			vow_bladeresonance = nbt.getBoolean("vow_bladeresonance");
			vow_smolderingbuildup = nbt.getBoolean("vow_smolderingbuildup");
			vow_concentratedinferno = nbt.getBoolean("vow_concentratedinferno");
			vow_desperateflames = nbt.getBoolean("vow_desperateflames");
			vow_pyroclasm = nbt.getBoolean("vow_pyroclasm");
			vow_meditativeopening = nbt.getBoolean("vow_meditativeopening");
			vow_emptyhandshrine = nbt.getBoolean("vow_emptyhandshrine");
			vow_overwhelmingmalevolence = nbt.getBoolean("vow_overwhelmingmalevolence");
			vow_shrinesupremacy = nbt.getBoolean("vow_shrinesupremacy");
			vow_overextendedmalice = nbt.getBoolean("vow_overextendedmalice");
			vow_distanceamplification = nbt.getBoolean("vow_distanceamplification");
			vow_stationaryperfection = nbt.getBoolean("vow_stationaryperfection");
			vow_calculatedwarp = nbt.getBoolean("vow_calculatedwarp");
			vow_overworlddominance = nbt.getBoolean("vow_overworlddominance");
			vow_unencumberedfocus = nbt.getBoolean("vow_unencumberedfocus");
			vow_pressuredexcellence = nbt.getBoolean("vow_pressuredexcellence");
			vow_fairweatherfighter = nbt.getBoolean("vow_fairweatherfighter");
			infinity_crush = nbt.getBoolean("infinity_crush");
			arm_translate_x = nbt.getDouble("arm_translate_x");
			arm_translate_y = nbt.getDouble("arm_translate_y");
			arm_translate_z = nbt.getDouble("arm_translate_z");
			arm_rotate_x = nbt.getDouble("arm_rotate_x");
			arm_rotate_y = nbt.getDouble("arm_rotate_y");
			arm_rotate_z = nbt.getDouble("arm_rotate_z");
			current_arm_animation = nbt.getString("current_arm_animation");
			arm_anim_holding = nbt.getDouble("arm_anim_holding");
			arm_anim_playing = nbt.getBoolean("arm_anim_playing");
			arm_anim_progress = nbt.getDouble("arm_anim_progress");
			arm_anim_start_tick = nbt.getDouble("arm_anim_start_tick");
			using_red = nbt.getBoolean("using_red");
			marked_entities = nbt.getString("marked_entities");
			marked_timestamps = nbt.getString("marked_timestamps");
			blue_fist_toggle = nbt.getBoolean("blue_fist_toggle");
			cleave_melee_toggle = nbt.getBoolean("cleave_melee_toggle");
			domain_image_1 = nbt.getDouble("domain_image_1");
			domain_image_2 = nbt.getDouble("domain_image_2");
			wcs_chant_progress = nbt.getDouble("wcs_chant_progress");
		}
	}

	public static class PlayerVariablesSyncMessage {
		private final PlayerVariables data;

		public PlayerVariablesSyncMessage(FriendlyByteBuf buffer) {
			this.data = new PlayerVariables();
			this.data.readNBT(buffer.readNbt());
		}

		public PlayerVariablesSyncMessage(PlayerVariables data) {
			this.data = data;
		}

		public static void buffer(PlayerVariablesSyncMessage message, FriendlyByteBuf buffer) {
			buffer.writeNbt((CompoundTag) message.data.writeNBT());
		}

		public static void handler(PlayerVariablesSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			context.enqueueWork(() -> {
				if (!context.getDirection().getReceptionSide().isServer()) {
					PlayerVariables variables = ((PlayerVariables) Minecraft.getInstance().player.getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
					variables.sorcerer = message.data.sorcerer;
					variables.wcs_x1 = message.data.wcs_x1;
					variables.wcs_y1 = message.data.wcs_y1;
					variables.wcs_z1 = message.data.wcs_z1;
					variables.wcs_x2 = message.data.wcs_x2;
					variables.wcs_y2 = message.data.wcs_y2;
					variables.wcs_z2 = message.data.wcs_z2;
					variables.charge_red = message.data.charge_red;
					variables.charge_blue = message.data.charge_blue;
					variables.charge_purple = message.data.charge_purple;
					variables.current_moveset = message.data.current_moveset;
					variables.RTC_unlocked = message.data.RTC_unlocked;
					variables.gojo_blue_unlocked = message.data.gojo_blue_unlocked;
					variables.wcs_power = message.data.wcs_power;
					variables.dismantle_barrage = message.data.dismantle_barrage;
					variables.red_flight = message.data.red_flight;
					variables.Output = message.data.Output;
					variables.Output_Dismantle = message.data.Output_Dismantle;
					variables.Output_Cleave = message.data.Output_Cleave;
					variables.Output_Flame = message.data.Output_Flame;
					variables.Output_Blue = message.data.Output_Blue;
					variables.Output_Red = message.data.Output_Red;
					variables.Output_Purple = message.data.Output_Purple;
					variables.Output_Domain = message.data.Output_Domain;
					variables.Output_HollowPurple = message.data.Output_HollowPurple;
					variables.Output_WorldSlash = message.data.Output_WorldSlash;
					variables.vow_overtime = message.data.vow_overtime;
					variables.vow_recoil = message.data.vow_recoil;
					variables.vow_wcsact = message.data.vow_wcsact;
					variables.vow_dismantleimbue = message.data.vow_dismantleimbue;
					variables.vow_RTChealbrain = message.data.vow_RTChealbrain;
					variables.vow_trueform = message.data.vow_trueform;
					variables.vow_executioner = message.data.vow_executioner;
					variables.vow_adaptivecleave = message.data.vow_adaptivecleave;
					variables.vow_shreddingbarrage = message.data.vow_shreddingbarrage;
					variables.vow_bladeresonance = message.data.vow_bladeresonance;
					variables.vow_smolderingbuildup = message.data.vow_smolderingbuildup;
					variables.vow_concentratedinferno = message.data.vow_concentratedinferno;
					variables.vow_desperateflames = message.data.vow_desperateflames;
					variables.vow_pyroclasm = message.data.vow_pyroclasm;
					variables.vow_meditativeopening = message.data.vow_meditativeopening;
					variables.vow_emptyhandshrine = message.data.vow_emptyhandshrine;
					variables.vow_overwhelmingmalevolence = message.data.vow_overwhelmingmalevolence;
					variables.vow_shrinesupremacy = message.data.vow_shrinesupremacy;
					variables.vow_overextendedmalice = message.data.vow_overextendedmalice;
					variables.vow_distanceamplification = message.data.vow_distanceamplification;
					variables.vow_stationaryperfection = message.data.vow_stationaryperfection;
					variables.vow_calculatedwarp = message.data.vow_calculatedwarp;
					variables.vow_overworlddominance = message.data.vow_overworlddominance;
					variables.vow_unencumberedfocus = message.data.vow_unencumberedfocus;
					variables.vow_pressuredexcellence = message.data.vow_pressuredexcellence;
					variables.vow_fairweatherfighter = message.data.vow_fairweatherfighter;
					variables.infinity_crush = message.data.infinity_crush;
					variables.arm_translate_x = message.data.arm_translate_x;
					variables.arm_translate_y = message.data.arm_translate_y;
					variables.arm_translate_z = message.data.arm_translate_z;
					variables.arm_rotate_x = message.data.arm_rotate_x;
					variables.arm_rotate_y = message.data.arm_rotate_y;
					variables.arm_rotate_z = message.data.arm_rotate_z;
					variables.current_arm_animation = message.data.current_arm_animation;
					variables.arm_anim_holding = message.data.arm_anim_holding;
					variables.arm_anim_playing = message.data.arm_anim_playing;
					variables.arm_anim_progress = message.data.arm_anim_progress;
					variables.arm_anim_start_tick = message.data.arm_anim_start_tick;
					variables.using_red = message.data.using_red;
					variables.marked_entities = message.data.marked_entities;
					variables.marked_timestamps = message.data.marked_timestamps;
					variables.blue_fist_toggle = message.data.blue_fist_toggle;
					variables.cleave_melee_toggle = message.data.cleave_melee_toggle;
					variables.domain_image_1 = message.data.domain_image_1;
					variables.domain_image_2 = message.data.domain_image_2;
					variables.wcs_chant_progress = message.data.wcs_chant_progress;
				}
			});
			context.setPacketHandled(true);
		}
	}
}
