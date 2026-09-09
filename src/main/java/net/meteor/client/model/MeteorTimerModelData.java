package net.meteor.client.model;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometría del Meteor Timer, traducida caja por caja del ModelMeteorTimer original
 * (textura 64x64, eje Y hacia arriba con origen en la esquina del bloque).
 * Partes: shape1 (cuerpo), base (placa), y 3 barras de elemento que giran.
 */
public final class MeteorTimerModelData {

	private MeteorTimerModelData() {}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("shape1",
				CubeListBuilder.create().texOffs(0, 36).addBox(0F, 0F, 0F, 16, 8, 16),
				PartPose.offset(0F, 0F, 0F));
		root.addOrReplaceChild("base",
				CubeListBuilder.create().texOffs(4, 0).addBox(0F, 0F, 0F, 14, 1, 14),
				PartPose.offset(1F, 8F, 1F));
		root.addOrReplaceChild("element_meteorite",
				CubeListBuilder.create().texOffs(0, 7).addBox(-2F, 0F, -1F, 1, 3, 2),
				PartPose.offset(8F, 10F, 8F));
		root.addOrReplaceChild("element_frezarite",
				CubeListBuilder.create().texOffs(9, 0).addBox(-4F, 0F, -1F, 1, 3, 2),
				PartPose.offset(8F, 10F, 8F));
		root.addOrReplaceChild("element_kreknorite",
				CubeListBuilder.create().texOffs(0, 0).addBox(6F, 0F, -1F, 1, 3, 2),
				PartPose.offset(8F, 10F, 8F));

		return LayerDefinition.create(mesh, 64, 64);
	}
}
