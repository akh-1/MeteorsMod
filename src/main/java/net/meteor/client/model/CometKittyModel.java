package net.meteor.client.model;

import net.minecraft.client.model.animal.feline.CatModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Modelo del Comet Kitty: la malla felina vanilla + un casco añadido como hijo
 * de la cabeza. La cabeza felina está en (0,15,-9), que coincide exactamente con
 * el punto de anclaje del casco en el mod original, así que con PartPose.ZERO
 * queda alineado y sigue la rotación de la cabeza igual que en 1.7.10.
 *
 * Se replican los nombres de parte vanilla porque el constructor de FelineModel
 * los busca por nombre.
 */
public class CometKittyModel extends CatModel {

	public CometKittyModel(ModelPart root) {
		super(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		CubeDeformation none = CubeDeformation.NONE;
		CubeDeformation tail2Def = new CubeDeformation(-0.02F);

		PartDefinition head = root.addOrReplaceChild("head",
				CubeListBuilder.create()
						.addBox("main", -2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 5.0F, none)
						.addBox("nose", -1.5F, -0.001F, -4.0F, 3, 2, 2, none, 0, 24)
						.addBox("ear1", -2.0F, -3.0F, 0.0F, 1, 1, 2, none, 0, 10)
						.addBox("ear2", 1.0F, -3.0F, 0.0F, 1, 1, 2, none, 6, 10),
				PartPose.offset(0.0F, 15.0F, -9.0F));

		// Casco (6 cubos) con las UVs del original, anclado a la cabeza.
		head.addOrReplaceChild("helmet",
				CubeListBuilder.create()
						.texOffs(34, 17).addBox(-3.5F, -4.0F, -5.0F, 7.0F, 7.0F, 8.0F, none)
						.texOffs(48, 12).addBox(-1.0F, -6.0F, -3.0F, 2.0F, 1.0F, 4.0F, none)
						.texOffs(14, 25).addBox(4.0F, -2.0F, -3.0F, 1.0F, 3.0F, 4.0F, none)
						.texOffs(24, 25).addBox(-5.0F, -2.0F, -3.0F, 1.0F, 3.0F, 4.0F, none)
						.texOffs(6, 29).addBox(-6.0F, -1.0F, -2.0F, 1.0F, 1.0F, 2.0F, none)
						.texOffs(0, 29).addBox(5.0F, -1.0F, -2.0F, 1.0F, 1.0F, 2.0F, none),
				PartPose.ZERO);

		root.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(20, 0).addBox(-2.0F, 3.0F, -8.0F, 4.0F, 16.0F, 6.0F, none),
				PartPose.offsetAndRotation(0.0F, 12.0F, -10.0F, ((float) Math.PI / 2F), 0.0F, 0.0F));
		root.addOrReplaceChild("tail1",
				CubeListBuilder.create().texOffs(0, 15).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, none),
				PartPose.offsetAndRotation(0.0F, 15.0F, 8.0F, 0.9F, 0.0F, 0.0F));
		root.addOrReplaceChild("tail2",
				CubeListBuilder.create().texOffs(4, 15).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, tail2Def),
				PartPose.offset(0.0F, 20.0F, 14.0F));

		CubeListBuilder hind = CubeListBuilder.create().texOffs(8, 13).addBox(-1.0F, 0.0F, 1.0F, 2.0F, 6.0F, 2.0F, none);
		root.addOrReplaceChild("left_hind_leg", hind, PartPose.offset(1.1F, 18.0F, 5.0F));
		root.addOrReplaceChild("right_hind_leg", hind, PartPose.offset(-1.1F, 18.0F, 5.0F));

		CubeListBuilder front = CubeListBuilder.create().texOffs(40, 0).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 10.0F, 2.0F, none);
		root.addOrReplaceChild("left_front_leg", front, PartPose.offset(1.2F, 14.1F, -5.0F));
		root.addOrReplaceChild("right_front_leg", front, PartPose.offset(-1.2F, 14.1F, -5.0F));

		return LayerDefinition.create(mesh, 64, 32);
	}

	/** Versión bebé: aplica el transformador felino (cabeza grande, cuerpo pequeño). */
	public static LayerDefinition createBabyBodyLayer() {
		return createBodyLayer().apply(net.minecraft.client.model.animal.feline.FelineModel.BABY_TRANSFORMER);
	}
}
