module telephonegame {
	requires javafx.controls;
	requires javafx.fxml;
	requires transitive javafx.graphics;
	requires javafx.base;

	opens fi.utu.tech.telephonegame to javafx.fxml;
	opens fi.utu.tech.telephonegame.ui to javafx.fxml;

	exports fi.utu.tech.telephonegame.ui;
	exports fi.utu.tech.telephonegame.network;
	exports fi.utu.tech.telephonegame;
}
