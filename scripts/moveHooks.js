function main() {
    return {
        beforeProcessMove: function (player) {
            Printer.printPlayerDetails(player);
        },
        afterProcessMove: function (player) {
        }
    }
}