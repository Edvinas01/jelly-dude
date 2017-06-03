function main() {
    return {

        /**
         * Makes so that player doesn't take health ticks.
         */
        beforeHealthTick: function (player, moved) {
            return false;
        }
    }
}