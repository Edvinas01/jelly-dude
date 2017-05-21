load('assets/scripts/Player.js');

function main() {

    var totalTime = 0;
    var interval = 1;
    var grow = true;

    /**
     * Grows and shrink the player constantly.
     */
    function shrinkAndGrow(player) {
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
    }

    return {
        beforeMove: function (player) {
            // shrinkAndGrow(player);
            // Player.fly(player);
        }
    }
}