/**
 * General player utilities.
 */
var Player = new function () {
    var deflation = Java.type('com.edd.jelly.behaviour.player.Player.Deflation');

    var totalTime = 0;
    var interval = 1;
    var grow = true;

    /**
     * Player deflation states.
     */
    this.Deflation = {
        IDLE: deflation.IDLE,
        WORKING: deflation.WORKING,
        DEFLATE: deflation.DEFLATE,
        INFLATE: deflation.INFLATE
    };

    /**
     * Grows the player.
     */
    this.grow = function (player) {
        player.deflationState = this.Deflation.INFLATE;
    };

    /**
     * Shrinks the player.
     */
    this.shrink = function (player) {
        player.deflationState = this.Deflation.DEFLATE;
    };

    /**
     * Makes the player fly.
     */
    this.fly = function (player) {
        player.airTime = 0;
    };

    /**
     * Constantly shrinks and grows the player.
     */
    this.shrinkAndGrow = function (player) {
        if (totalTime > interval) {
            totalTime = 0;
            grow = !grow;
        }

        if (grow) {
            Player.grow(player);
        } else {
            Player.shrink(player);
        }

        totalTime += game.deltaTime;
    };
};