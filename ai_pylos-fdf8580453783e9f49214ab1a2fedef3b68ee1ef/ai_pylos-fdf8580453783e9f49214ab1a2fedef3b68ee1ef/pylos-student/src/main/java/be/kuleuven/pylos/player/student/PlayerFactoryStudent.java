package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.player.PylosPlayer;
import be.kuleuven.pylos.player.PylosPlayerFactory;
import be.kuleuven.pylos.player.PylosPlayerType;

/**
 * Created by Jan on 20/02/2015.
 */
public class PlayerFactoryStudent extends PylosPlayerFactory {

	public PlayerFactoryStudent() {
		super("Student");
	}

	@Override
	protected void createTypes() {

		/* example */
		add(new PylosPlayerType("Student previous version") {
			@Override
			public PylosPlayer create() {
				return new StudentPlayerPrevious();
			}
		});

		add(new PylosPlayerType("Student New version") {
			@Override
			public PylosPlayer create() {
				return new StudentPlayerNew();
			}
		});

		add(new PylosPlayerType("Student - Random") {
			@Override
			public PylosPlayer create() {
				return new StudentPlayerRandomFit();
			}
		});

		add(new PylosPlayerType("Student - V1") {
			@Override
			public PylosPlayer create() {
				return new StudentPlayerV1();
			}
		});

		add(new PylosPlayerType("Student - V2") {
			@Override
			public PylosPlayer create() {
				return new StudentPlayerV2();
			}
		});
		add(new PylosPlayerType("Student - V3") {
			@Override
			public PylosPlayer create() {
				return new StudentPlayerV3();
			}
		});
		add(new PylosPlayerType("Student - V4") {
			@Override
			public PylosPlayer create() {
				return new StudentPlayerV4();
			}
		});
		add(new PylosPlayerType("Student - V5") {
			@Override
			public PylosPlayer create() {
				return new StudentPlayerV5();
			}
		});
	}
}
