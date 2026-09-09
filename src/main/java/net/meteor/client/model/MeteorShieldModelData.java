package net.meteor.client.model;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometría del Meteor Shield, traducida caja por caja del ModelMeteorShield original
 * (textura 128x64). Convención Y-abajo: se renderiza con flip 180° en Z y traslación
 * a (0.5, 1.5, 0.5), como el TESR original.
 */
public final class MeteorShieldModelData {

	private MeteorShieldModelData() {}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("base_shape", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-6F, 0F, -6F, 12, 0, 12), PartPose.offset(0F, 15F, 0F));
		root.addOrReplaceChild("top_layer", CubeListBuilder.create().texOffs(0, 14).mirror().addBox(-5F, -8F, -5F, 10, 1, 10), PartPose.offset(0F, 15F, 0F));
		root.addOrReplaceChild("middle_layer", CubeListBuilder.create().texOffs(0, 26).mirror().addBox(-4F, -10F, -4F, 8, 1, 8), PartPose.offset(0F, 15F, 0F));
		root.addOrReplaceChild("bottom_layer", CubeListBuilder.create().texOffs(0, 35).mirror().addBox(-3F, -12F, -3F, 6, 1, 6), PartPose.offset(0F, 15F, 0F));

		root.addOrReplaceChild("side1", CubeListBuilder.create().texOffs(66, 0).mirror().addBox(-7F, -7F, -7F, 14, 10, 1), PartPose.offset(0F, 15F, 0F));
		root.addOrReplaceChild("side2", CubeListBuilder.create().texOffs(66, 0).mirror().addBox(-7F, -7F, 6F, 14, 10, 1), PartPose.offset(0F, 15F, 0F));
		root.addOrReplaceChild("side3", CubeListBuilder.create().texOffs(83, 0).mirror().addBox(-7F, -7F, -7F, 1, 10, 14), PartPose.offset(0F, 15F, 0F));
		root.addOrReplaceChild("side4", CubeListBuilder.create().texOffs(83, 0).mirror().addBox(6F, -7F, -7F, 1, 10, 14), PartPose.offset(0F, 15F, 0F));

		root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-0.5F, 0F, -0.5F, 1, 6, 1), PartPose.offsetAndRotation(-6F, 18F, 6F, 0.2617994F, -0.7853982F, 0F));
		root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-0.5F, 0F, -0.5F, 1, 6, 1), PartPose.offsetAndRotation(6F, 18F, 6F, 0.2617994F, 0.7853982F, 0F));
		root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-0.5F, 0F, -0.5F, 1, 6, 1), PartPose.offsetAndRotation(6F, 18F, -6F, -0.2617994F, -0.7853982F, 0F));
		root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-0.5F, 0F, -0.5F, 1, 6, 1), PartPose.offsetAndRotation(-6F, 18F, -6F, -0.2617994F, 0.7853982F, 0F));

		root.addOrReplaceChild("foot1", CubeListBuilder.create().texOffs(52, 0).mirror().addBox(-0.5F, 0F, -0.5F, 1, 1, 2), PartPose.offsetAndRotation(-7F, 23F, 7F, 0F, -0.7853982F, 0F));
		root.addOrReplaceChild("foot2", CubeListBuilder.create().texOffs(52, 0).mirror().addBox(-0.5F, 0F, -0.5F, 1, 1, 2), PartPose.offsetAndRotation(7F, 23F, 7F, 0F, 0.7853982F, 0F));
		root.addOrReplaceChild("foot3", CubeListBuilder.create().texOffs(52, 0).mirror().addBox(-0.5F, 0F, -0.5F, 1, 1, 2), PartPose.offsetAndRotation(-7F, 23F, -7F, 0F, -2.356194F, 0F));
		root.addOrReplaceChild("foot4", CubeListBuilder.create().texOffs(52, 0).mirror().addBox(-0.5F, 0F, -0.5F, 1, 1, 2), PartPose.offsetAndRotation(7F, 23F, -7F, 0F, 2.356194F, 0F));

		root.addOrReplaceChild("inner_slope1", CubeListBuilder.create().texOffs(32, 26).mirror().addBox(-6F, 1F, 1F, 12, 0, 7), PartPose.offsetAndRotation(0F, 15F, 0F, 0.8203047F, 0F, 0F));
		root.addOrReplaceChild("inner_slope2", CubeListBuilder.create().texOffs(32, 26).mirror().addBox(-6F, 1F, -8F, 12, 0, 7), PartPose.offsetAndRotation(0F, 15F, 0F, -0.8203047F, 0F, 0F));
		root.addOrReplaceChild("inner_slope3", CubeListBuilder.create().texOffs(32, 14).mirror().addBox(-8F, 1F, -6F, 7, 0, 12), PartPose.offsetAndRotation(0F, 15F, 0F, 0F, 0F, 0.8203047F));
		root.addOrReplaceChild("inner_slope4", CubeListBuilder.create().texOffs(32, 14).mirror().addBox(1F, 1F, -6F, 7, 0, 12), PartPose.offsetAndRotation(0F, 15F, 0F, 0F, 0F, -0.8203047F));

		root.addOrReplaceChild("side_panel1", CubeListBuilder.create().texOffs(32, 34).mirror().addBox(6F, 1F, -2F, 1, 4, 4), PartPose.offsetAndRotation(0F, 12F, 0F, 0F, 0F, -0.2617994F));
		root.addOrReplaceChild("side_panel2", CubeListBuilder.create().texOffs(32, 34).mirror().addBox(-7F, 1F, -2F, 1, 4, 4), PartPose.offsetAndRotation(0F, 12F, 0F, 0F, 0F, 0.2617994F));

		return LayerDefinition.create(mesh, 128, 64);
	}
}
