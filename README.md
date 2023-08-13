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


#### The first method that will be covered is the `applyToAll()` method:
```java
public boolean applyToAll(World world, BlockState state, BlockPos pos, BiFunction<BlockPos, Vec3i, Boolean> function)
```

This method takes in a `World`, `BlockState`, `BlockPos`, and `Bifunction`.

This method will take code from the BiFunction, and apply it to every block that makes up the multiblock.

An example usage would be:
```java
@Override
public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
    if (world.isClient() && hand == Hand.MAIN_HAND) {
            this.applyToAll(world, state, pos, (worldPos, relativePos) -> {
                player.sendMessage(Text.literal("Hello from: " + relativePos));
                return true;
            });
        }
    return super.onUse(state, world, pos, player, hand, hit);
}
```

Here we just make sure that the use method is only called on the client, and when the hand is the main hand.
Then we call the `applyToAll()` method, passing in the `World`, `BlockState`, and `BlockPos` given by the `onUse()` method.
Then for the last parameter, create a BiFunction where we will put our code.

The BiFunction will give you two variables, the position of the current block in the world, and the position of the block relative to the multiblock structure.
In this example, the relative position is sent in a message to the player, this will result in the relative positions of all blocks in the multiblock being output to the player.

Thats pretty much all there is to the `applyToAll()` method, you can get way more complex in usage, but this example should be enough to get you started.


#### The second method we will go over is the `applyToMain()` method:
```java
boolean applyToMain(World world, BlockPos pos, BiFunction<BlockState, BlockPos, Boolean> function)
```

This method takes in a `World`, `BlockPos`, and `Bifunction`.

This method will take code from the BiFunction, and apply it to the main block, that way, no matter what part of the multiblock you click, it will always redirect to the main block.

An example usage would be:
```java
@Override
public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
    if (world.isClient() && hand == Hand.MAIN_HAND) {
        this.applyToMain(world, pos, (mainState, mainPos) -> {
            player.sendMessage(Text.literal("Hello from: " + mainPos));
            return true;
        });
    }

    return super.onUse(state, world, pos, player, hand, hit);
}
```

As you can see, the usage of the method is very similar to the `applyToAll()` method, however this will only be running on the 1 main block instead of every single block.

This time, the BiFunction will provide the `BlockState` and `BlockPos` for the main block.
This should allow you to modify/use the block however you need, in this case, we just message the main block position to the player.

### Complex Shapes

Complex shapes are shapes that you will define block-by-block within a certain area.

To get started with complex shapes, we will go back to the `MultiblockPositioner` and its Builder.

```java
public static final Block DUMPSTER_MULTI_BLOCK = register("multi_block", new DumpsterMultiblock(
    AbstractBlock.Settings.of(Material.METAL).strength(4),
    new MultiblockPositioner.Builder(
            new Vec3i(5, 5, 5),
            new ArrayList<>(List.of(
                    new Point(2, 1, 2),
                    new Point(2, 2, 2),
                    new Point(2, 1, 1),
                    new Point(1, 1, 2),
                    new Point(2, 1, 3),
                    new Point(3, 1, 2)))
    ).build()
));
```
