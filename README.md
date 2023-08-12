# SuperBlockLib
## Overview
SuperBlockLib is a library for mod devs that want to create customizable multiblocks easily.

You can shape it how you like, from simple box shapes to complex, point-to-point shapes.

## Installation
WIP

# Documentaion
This will assume that you already know how blocks and block entities work, if not, I recommend checking the fabric wiki for more info

## Getting Started
To create your first multiblock, you will need to create two classes, that extend `MultiBlockEntity` and `MultiBlock` respectively.

Your classes should look somewhat like this:

`CustomMultiblock` class:
```java
public class CustomMultiblock extends MultiBlock {
    public CustomMultiblock(Settings settings, MultiblockPositioner positioner) {
        super(settings, positioner);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CustomMultiblockEntity(pos, state);
    }
}
```

Make sure to create your block entity registry and replace `BlockEntityRegistry.CUSTOM_MULTI_BLOCK_ENTITY` with your block entity registry.
`CustomMultiblockEntity` class:
```java
public class CustomMultiblockEntity extends MultiBlockEntity {
    public CustomMultiblockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.CUSTOM_MULTI_BLOCK_ENTITY, pos, state);
    }
}
```

Now that you have your basic files, you can now move on to your block registry. This is where most of the building will be.
In your block registry, you will, for the most part, be creating a normal block.

```java
public static final Block CUSTOM_MULTI_BLOCK = register("custom_multi_block", new CustomMultiblock(
    AbstractBlock.Settings.of(Material.METAL).strength(4),
    new MultiblockPositioner.Builder(new BlockPos(3, 2, 2)).build()
));
```

As you can see, it's very similar to a normal block, however, we do have something additional which is the `MultiblockPositioner`.

This class comes with a builder, where you will define different properties about the shape, size, and offset of both the multiblock, and model.
For simplicity, we will just define the size of the multiblock, which currently, is just a default box with a size of 3x2x2.

Now for a basic multiblock, that's basically it, now you just add your model and textures the same way you would any other block.

> One thing to note, once you're in-game, the model will default to the front-right corner facing the player, you are able to offset the model which I will explain later on.

Once everything is implemented, boot up your game and check out the results!

## Complex Multiblocks
Now that you have a basic understanding of how the library works, we can move on to more complex stuff such as handling interactions with some handy helper methods and creating complex shapes!

### Adding Interactions
When adding interactions to a multiblock, there are two methods you may want to know of, `applyToAll()` and `applyToMain()`.

These methods will be found in the `MultiBlock` class, and they will be used to decide how the interactions behave.

The first method that will be covered is the `applyToAll()` method:
```java
public final boolean applyToAll(World world, BlockState state, BlockPos pos, BiFunction<BlockPos, Vec3i, Boolean> function)
```

This method takes in a `World`, `BlockState`, `BlockPos`, and `Bifunction`.

This method takes whatever code you put in the `Bifunction` and applies it to every single block that makes up the multiblock.

An example usage would be:
```java
@Override
public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
    // Get the position of the core block.
    MultiBlockEntity entity = (MultiBlockEntity) world.getBlockEntity(pos);
    BlockPos mainBlock = entity != null ? entity.getMainBlock() : null;

    // Now make sure the main block isn't null and then we use the method
    if (mainBlock != null) {
        this.applyToAll(world, state, mainBlock, (worldPos, relativePos) -> {
            return true;
        });
    }
    return super.onUse(state, world, pos, player, hand, hit);
}
```
