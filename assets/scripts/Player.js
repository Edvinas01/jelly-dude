/**
 * General player utilities.
 */
var Player = new function () {
    var deflation = Java.type('com.edd.jelly.behaviour.player.Player.Deflation');

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
};