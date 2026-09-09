package net.meteor.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.monster.creeper.CreeperModel;

/**
 * Modelo del Creeper Alienígena: el modelo del creeper vanilla + una antena
 * añadida como hijo de la cabeza (se anima con ella automáticamente).
 *
 * Reusa los nombres de parte vanilla ("head", "body", las 4 patas) porque el
 * constructor de {@link CreeperModel} los busca por nombre. La antena va como
 * hijo "antenna" de "head" con las coordenadas/UV del mod original.
 */
public class AlienCreeperModel extends CreeperModel {

	public AlienCreeperModel(ModelPart root) {
		super(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		CubeDeformation none = CubeDeformation.NONE;

		PartDefinition head = root.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, none),
				PartPose.offset(0.0F, 6.0F, 0.0F));

		// Antena: tallo (1x6x1) + bulbo (2x2x2), en el espacio local de la cabeza.
		head.addOrReplaceChild("antenna",
				CubeListBuilder.create()
						.texOffs(33, 6).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 6.0F, 1.0F, none)
						.texOffs(33, 1).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 2.0F, 2.0F, none),
				PartPose.ZERO);

		root.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, none),
				PartPose.offset(0.0F, 6.0F, 0.0F));

		CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, none);
		root.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(-2.0F, 18.0F, 4.0F));
		root.addOrReplaceChild("left_hind_leg", leg, PartPose.offset(2.0F, 18.0F, 4.0F));
		root.addOrReplaceChild("right_front_leg", leg, PartPose.offset(-2.0F, 18.0F, -4.0F));
		root.addOrReplaceChild("left_front_leg", leg, PartPose.offset(2.0F, 18.0F, -4.0F));

		return LayerDefinition.create(mesh, 64, 32);
	}
}
