# TankStar
This is a tank game made by Java and processing library. Use left and right keys to move the tank, use up and down keys to move the turret, use W and S keys to increase/decrease the turret power, press spacebar to shoot a projectile, and once shot turn switches. For strategic items, press f to buy extra fuel(cost: 10), press x to buy a large projectile(cost: 20), press r to buy repair kit(cost: 20), press p to buy extra parachutes(cost: 15)


Once a projectile explodes, the terrain will be destroyed. If a tank happens to be within the explosion radius, it will receive damage proportional to the distance between its center coordinates and the explosion center coordinates. The tank causing the damage will earn corresponding points.

Additionally, when a tank's parachute is exhausted, it will suffer fall damage. If the fall damage is caused by another tank, the other tank will earn corresponding points.
