package net.querz.mcaselector.ui.dialog;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import net.querz.mcaselector.Config;
import net.querz.mcaselector.io.WorldDirectories;
import net.querz.mcaselector.property.DataProperty;
import net.querz.mcaselector.text.Translation;
import net.querz.mcaselector.ui.FileTextField;
import net.querz.mcaselector.ui.TileMapBox;
import net.querz.mcaselector.ui.UIFactory;

import javax.swing.*;
import java.io.File;
import java.util.*;

public class SettingsDialog extends Dialog<SettingsDialog.Result> {

	/*
	* Region selection color and opacity
	* Chunk selection color and opacity
	* MCAFilePipe thread options:
	* - Number of threads for file reading
	* - Number of threads for processing
	* - Number of threads for writing
	* - Maximum amount of loaded files
	* toggle shading
	* toggle shading of water
	* toggle showing non-existent regions
	* toggle smooth rendering
	* toggle smooth overlays
	* background pattern
	* minecraft saves folder
	* toggle debug
	* */

	private static final int processorCount = Runtime.getRuntime().availableProcessors();
	private static final long maxMemory = Runtime.getRuntime().maxMemory();

	private final TabPane tabPane = new TabPane();

	private final ComboBox<Locale> languages = new ComboBox<>();

	private final Slider readThreadsSlider = createSlider(1, processorCount, 1, Config.getLoadThreads());
	private final Slider processThreadsSlider = createSlider(1, processorCount * 2, 1, Config.getProcessThreads());
	private final Slider writeThreadsSlider = createSlider(1, processorCount, 1, Config.getWriteThreads());
	private final Slider maxLoadedFilesSlider = createSlider(1, (int) Math.max(Math.ceil(maxMemory / 1_000_000_000D) * 6, 4), 1, Config.getMaxLoadedFiles());
	private final Slider heightSlider = new Slider(-64, 319, 319);
	private final CheckBox layerOnly = new CheckBox();
	private final CheckBox caves = new CheckBox();
	private final Button regionSelectionColorPreview = new Button();
	private final Button chunkSelectionColorPreview = new Button();
	private final Button pasteChunksColorPreview = new Button();
	private final CheckBox shadeCheckBox = new CheckBox();
	private final CheckBox shadeWaterCheckBox = new CheckBox();
	private final CheckBox showNonexistentRegionsCheckBox = new CheckBox();
	private final CheckBox smoothRendering = new CheckBox();
	private final CheckBox smoothOverlays = new CheckBox();
	private final ComboBox<TileMapBox.TileMapBoxBackground> tileMapBackgrounds = new ComboBox<>();
	private final FileTextField mcSavesDir = new FileTextField();
	private final CheckBox debugCheckBox = new CheckBox();
	private final FileTextField poiField = new FileTextField();
	private final FileTextField entitiesField = new FileTextField();

	private Color regionSelectionColor = Config.getRegionSelectionColor().makeJavaFXColor();
	private Color chunkSelectionColor = Config.getChunkSelectionColor().makeJavaFXColor();
	private Color pasteChunksColor = Config.getPasteChunksColor().makeJavaFXColor();

	private final ButtonType reset = new ButtonType(Translation.DIALOG_SETTINGS_RESET.toString(), ButtonBar.ButtonData.LEFT);

	public SettingsDialog(Stage primaryStage, boolean renderSettings) {
		titleProperty().bind(Translation.DIALOG_SETTINGS_TITLE.getProperty());
		initStyle(StageStyle.UTILITY);
		getDialogPane().getStyleClass().add("settings-dialog-pane");
		getDialogPane().getScene().getStylesheets().addAll(primaryStage.getScene().getStylesheets());
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, reset);

		getDialogPane().lookupButton(reset).addEventFilter(ActionEvent.ACTION, e -> {
			e.consume();
			languages.setValue(Config.DEFAULT_LOCALE);
			readThreadsSlider.setValue(Config.DEFAULT_LOAD_THREADS);
			processThreadsSlider.setValue(Config.DEFAULT_PROCESS_THREADS);
			writeThreadsSlider.setValue(Config.DEFAULT_WRITE_THREADS);
			maxLoadedFilesSlider.setValue(Config.DEFAULT_MAX_LOADED_FILES);
			regionSelectionColor = Config.DEFAULT_REGION_SELECTION_COLOR.makeJavaFXColor();
			regionSelectionColorPreview.setBackground(new Background(new BackgroundFill(Config.DEFAULT_REGION_SELECTION_COLOR.makeJavaFXColor(), CornerRadii.EMPTY, Insets.EMPTY)));
			chunkSelectionColor = Config.DEFAULT_CHUNK_SELECTION_COLOR.makeJavaFXColor();
			chunkSelectionColorPreview.setBackground(new Background(new BackgroundFill(Config.DEFAULT_CHUNK_SELECTION_COLOR.makeJavaFXColor(), CornerRadii.EMPTY, Insets.EMPTY)));
			pasteChunksColor = Config.DEFAULT_PASTE_CHUNKS_COLOR.makeJavaFXColor();
			pasteChunksColorPreview.setBackground(new Background(new BackgroundFill(Config.DEFAULT_PASTE_CHUNKS_COLOR.makeJavaFXColor(), CornerRadii.EMPTY, Insets.EMPTY)));
			shadeCheckBox.setSelected(Config.DEFAULT_SHADE);
			shadeWaterCheckBox.setSelected(Config.DEFAULT_SHADE_WATER);
			showNonexistentRegionsCheckBox.setSelected(Config.DEFAULT_SHOW_NONEXISTENT_REGIONS);
			smoothRendering.setSelected(Config.DEFAULT_SMOOTH_RENDERING);
			smoothOverlays.setSelected(Config.DEFAULT_SMOOTH_OVERLAYS);
			tileMapBackgrounds.setValue(TileMapBox.TileMapBoxBackground.valueOf(Config.DEFAULT_TILEMAP_BACKGROUND));
			mcSavesDir.setFile(Config.DEFAULT_MC_SAVES_DIR == null ? null : new File(Config.DEFAULT_MC_SAVES_DIR));
			debugCheckBox.setSelected(Config.DEFAULT_DEBUG);
		});

		setResultConverter(c -> {
			if (c.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
				return new Result(
					languages.getSelectionModel().getSelectedItem(),
					(int) readThreadsSlider.getValue(),
					(int) processThreadsSlider.getValue(),
					(int) writeThreadsSlider.getValue(),
					(int) maxLoadedFilesSlider.getValue(),
					regionSelectionColor,
					chunkSelectionColor,
					pasteChunksColor,
					shadeCheckBox.isSelected(),
					shadeWaterCheckBox.isSelected(),
					showNonexistentRegionsCheckBox.isSelected(),
					smoothRendering.isSelected(),
					smoothOverlays.isSelected(),
					tileMapBackgrounds.getSelectionModel().getSelectedItem(),
					mcSavesDir.getFile(),
					debugCheckBox.isSelected(),
					(int) heightSlider.getValue(),
					layerOnly.isSelected(),
					caves.isSelected(),
					poiField.getFile(),
					entitiesField.getFile()
				);
			}
			return null;
		});

		languages.getItems().addAll(Translation.getAvailableLanguages());
		languages.setValue(Config.getLocale());
		languages.setConverter(new StringConverter<>() {

			final Map<String, Locale> cache = new HashMap<>();

			@Override
			public String toString(Locale locale) {
				String display = locale.getDisplayName(locale);
				cache.put(display, locale);
				return display;
			}

			@Override
			public Locale fromString(String string) {
				return cache.get(string);
			}
		});
		languages.getStyleClass().add("languages-combo-box");

		regionSelectionColorPreview.getStyleClass().clear();
		chunkSelectionColorPreview.getStyleClass().clear();
		pasteChunksColorPreview.getStyleClass().clear();
		regionSelectionColorPreview.getStyleClass().add("color-preview-button");
		chunkSelectionColorPreview.getStyleClass().add("color-preview-button");
		pasteChunksColorPreview.getStyleClass().add("color-preview-button");
		regionSelectionColorPreview.setBackground(new Background(new BackgroundFill(regionSelectionColor, CornerRadii.EMPTY, Insets.EMPTY)));
		chunkSelectionColorPreview.setBackground(new Background(new BackgroundFill(chunkSelectionColor, CornerRadii.EMPTY, Insets.EMPTY)));
		pasteChunksColorPreview.setBackground(new Background(new BackgroundFill(pasteChunksColor, CornerRadii.EMPTY, Insets.EMPTY)));
		shadeCheckBox.setSelected(Config.shade());
		shadeWaterCheckBox.setSelected(Config.shadeWater());
		showNonexistentRegionsCheckBox.setSelected(Config.showNonExistentRegions());
		smoothRendering.setSelected(Config.smoothRendering());
		smoothOverlays.setSelected(Config.smoothOverlays());
		tileMapBackgrounds.getItems().addAll(TileMapBox.TileMapBoxBackground.values());

		tileMapBackgrounds.setCellFactory((listView) -> {
			ListCell<TileMapBox.TileMapBoxBackground> cell = new ListCell<>() {

				@Override
				public void updateIndex(int i) {
					super.updateIndex(i);
					TileMapBox.TileMapBoxBackground[] values = TileMapBox.TileMapBoxBackground.values();
					if (i < 0 || i >= values.length) {
						return;
					}
					setBackground(values[i].getBackground());
				}
			};
			// we don't want this to be treated like a regular list cell
			cell.getStyleClass().clear();
			return cell;
		});
		tileMapBackgrounds.setButtonCell(tileMapBackgrounds.getCellFactory().call(null));
		tileMapBackgrounds.getStyleClass().add("tilemap-backgrounds-combo-box");

		tileMapBackgrounds.setValue(TileMapBox.TileMapBoxBackground.valueOf(Config.getTileMapBackground()));
		mcSavesDir.setFile(Config.getMCSavesDir() == null ? null : new File(Config.getMCSavesDir()));
		debugCheckBox.setSelected(Config.debug());

		regionSelectionColorPreview.setOnMousePressed(e -> {
			Optional<Color> result = new ColorPicker(getDialogPane().getScene().getWindow(), regionSelectionColor).showColorPicker();
			result.ifPresent(c -> {
				regionSelectionColor = c;
				regionSelectionColorPreview.setBackground(new Background(new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY)));
			});
		});
		chunkSelectionColorPreview.setOnMousePressed(e -> {
			Optional<Color> result = new ColorPicker(getDialogPane().getScene().getWindow(), chunkSelectionColor).showColorPicker();
			result.ifPresent(c -> {
				chunkSelectionColor = c;
				chunkSelectionColorPreview.setBackground(new Background(new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY)));
			});
		});
		pasteChunksColorPreview.setOnMousePressed(e -> {
			Optional<Color> result = new ColorPicker(getDialogPane().getScene().getWindow(), pasteChunksColor).showColorPicker();
			result.ifPresent(c -> {
				pasteChunksColor = c;
				pasteChunksColorPreview.setBackground(new Background(new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY)));
			});
		});

		shadeCheckBox.setOnAction(e -> shadeWaterCheckBox.setDisable(!shadeCheckBox.isSelected()));
		shadeWaterCheckBox.setDisable(!shadeCheckBox.isSelected());

		layerOnly.setOnAction(e -> caves.setDisable(layerOnly.isSelected()));
		caves.setDisable(layerOnly.isSelected());
		layerOnly.setDisable(caves.isSelected());
		caves.setOnAction(e -> layerOnly.setDisable(caves.isSelected()));

		HBox debugBox = new HBox();
		debugBox.getStyleClass().add("debug-box");
		Hyperlink logFileLink = UIFactory.explorerLink(Translation.DIALOG_SETTINGS_SHOW_LOG_FILE, Config.getLogFile().getParentFile(), null);
		debugBox.getChildren().addAll(debugCheckBox, logFileLink);

		if (Config.getWorldDirs() != null) {
			WorldDirectories worldDirectories = Config.getWorldDirs().clone();
			poiField.setFile(worldDirectories.getPoi());
			entitiesField.setFile(worldDirectories.getEntities());
		}

		heightSlider.setValue(Config.getRenderHeight());
		heightSlider.setSnapToTicks(true);
		heightSlider.setMajorTickUnit(64);
		heightSlider.setMinorTickCount(384);
		heightSlider.setBlockIncrement(1);

		// -------------------------------------------------------------------------------------------------------------

		Tab globalTab = createTab("Global");
		VBox globalBox = new VBox();

		GridPane languageGrid = createGrid();
		addPairToGrid(languageGrid, 0, UIFactory.label(Translation.DIALOG_SETTINGS_LANGUAGE), languages);
		BorderedTitledPane lang = new BorderedTitledPane(Translation.DIALOG_SETTINGS_LANGUAGE, languageGrid);

		GridPane selectionsGrid = createGrid();
		addPairToGrid(selectionsGrid, 0, UIFactory.label(Translation.DIALOG_SETTINGS_REGION_COLOR), regionSelectionColorPreview);
		addPairToGrid(selectionsGrid, 1, UIFactory.label(Translation.DIALOG_SETTINGS_CHUNK_COLOR), chunkSelectionColorPreview);
		addPairToGrid(selectionsGrid, 2, UIFactory.label(Translation.DIALOG_SETTINGS_PASTED_CHUNKS_COLOR), pasteChunksColorPreview);
		BorderedTitledPane selections = new BorderedTitledPane(Translation.MENU_SELECTION, selectionsGrid);

		GridPane miscGrid = createGrid();
		addPairToGrid(miscGrid, 0, UIFactory.label(Translation.DIALOG_SETTINGS_MC_SAVES_DIR), mcSavesDir);
		addPairToGrid(miscGrid, 1, UIFactory.label(Translation.DIALOG_SETTINGS_PRINT_DEBUG), debugBox);
		BorderedTitledPane misc = new BorderedTitledPane(Translation.DIALOG_SETTINGS_LANGUAGE, miscGrid);

		globalBox.getChildren().addAll(lang, selections, misc);
		globalTab.setContent(globalBox);

		Tab processingTab = createTab("Processing");
		VBox processingBox = new VBox();

		GridPane threadGrid = createGrid();
		addPairToGrid(threadGrid, 0, UIFactory.label(Translation.DIALOG_SETTINGS_READ_THREADS), readThreadsSlider, UIFactory.attachTextFieldToSlider(readThreadsSlider));
		addPairToGrid(threadGrid, 1, UIFactory.label(Translation.DIALOG_SETTINGS_PROCESS_THREADS), processThreadsSlider, UIFactory.attachTextFieldToSlider(processThreadsSlider));
		addPairToGrid(threadGrid, 2, UIFactory.label(Translation.DIALOG_SETTINGS_WRITE_THREADS), writeThreadsSlider, UIFactory.attachTextFieldToSlider(writeThreadsSlider));
		BorderedTitledPane threads = new BorderedTitledPane(Translation.DIALOG_SETTINGS_PROCESS_THREADS, threadGrid);

		GridPane filesGrid = createGrid();
		addPairToGrid(filesGrid, 0, UIFactory.label(Translation.DIALOG_SETTINGS_MAX_FILES), maxLoadedFilesSlider, UIFactory.attachTextFieldToSlider(maxLoadedFilesSlider));
		BorderedTitledPane files = new BorderedTitledPane(Translation.DIALOG_SETTINGS_MAX_FILES, filesGrid);

		processingBox.getChildren().addAll(threads, files);
		processingTab.setContent(processingBox);

		Tab renderingTab = createTab("Rendering");
		VBox renderingBox = new VBox();

		HBox shadingAndSmooth = new HBox();

		GridPane shadingGrid = createGrid();
		addPairToGrid(shadingGrid, 0, UIFactory.label(Translation.DIALOG_SETTINGS_SHADE), shadeCheckBox);
		addPairToGrid(shadingGrid, 1, UIFactory.label(Translation.DIALOG_SETTINGS_SHADE_WATER), shadeWaterCheckBox);
		BorderedTitledPane shade = new BorderedTitledPane(Translation.DIALOG_SETTINGS_SHADE, shadingGrid);

		GridPane smoothGrid = createGrid();
		addPairToGrid(smoothGrid, 0, UIFactory.label(Translation.DIALOG_SETTINGS_SMOOTH_RENDERING), smoothRendering);
		addPairToGrid(smoothGrid, 1, UIFactory.label(Translation.DIALOG_SETTINGS_SMOOTH_OVERLAYS), smoothOverlays);
		BorderedTitledPane smooth = new BorderedTitledPane(Translation.DIALOG_SETTINGS_SMOOTH_RENDERING, smoothGrid);

		HBox.setHgrow(shade, Priority.ALWAYS);
		HBox.setHgrow(smooth, Priority.ALWAYS);
		shadingAndSmooth.getChildren().addAll(shade, smooth);

		GridPane layerGrid = createGrid();
		addPairToGrid(layerGrid, 0, UIFactory.label(Translation.DIALOG_WORLD_SETTINGS_RENDER_HEIGHT), heightSlider, UIFactory.attachTextFieldToSlider(heightSlider));
		addPairToGrid(layerGrid, 1, UIFactory.label(Translation.DIALOG_WORLD_SETTINGS_RENDER_LAYER_ONLY), layerOnly);
		addPairToGrid(layerGrid, 2, UIFactory.label(Translation.DIALOG_WORLD_SETTINGS_RENDER_CAVES), caves);
		BorderedTitledPane layers = new BorderedTitledPane(Translation.DIALOG_SETTINGS_SMOOTH_RENDERING, layerGrid);

		GridPane backgroundGrid = createGrid();
		addPairToGrid(backgroundGrid, 0, UIFactory.label(Translation.DIALOG_SETTINGS_BACKGROUND_PATTERN), tileMapBackgrounds);
		addPairToGrid(backgroundGrid, 1, UIFactory.label(Translation.DIALOG_SETTINGS_SHOW_NONEXISTENT_REGIONS), showNonexistentRegionsCheckBox);
		BorderedTitledPane background = new BorderedTitledPane(Translation.DIALOG_SETTINGS_BACKGROUND_PATTERN, backgroundGrid);

		renderingBox.getChildren().addAll(shadingAndSmooth, layers, background);
		renderingTab.setContent(renderingBox);

		Tab worldTab = createTab("World");
		VBox worldBox = new VBox();

		GridPane worldGrid = createGrid();
		addPairToGrid(worldGrid, 0, UIFactory.label(Translation.DIALOG_WORLD_SETTINGS_POI), poiField);
		addPairToGrid(worldGrid, 1, UIFactory.label(Translation.DIALOG_WORLD_SETTINGS_ENTITIES), entitiesField);
		BorderedTitledPane world = new BorderedTitledPane(Translation.DIALOG_SETTINGS_SHADE, worldGrid);

		worldBox.getChildren().addAll(world);
		worldTab.setContent(worldBox);

		// -------------------------------------------------------------------------------------------------------------

		renderingTab.setDisable(Config.getWorldDirs() == null);
		worldTab.setDisable(Config.getWorldDirs() == null);

		tabPane.getTabs().addAll(globalTab, processingTab, renderingTab, worldTab);

		final DataProperty<Tab> focusedTab = new DataProperty<>(globalTab);
		if (Config.getWorldDirs() != null && renderSettings) {
			tabPane.getSelectionModel().select(renderingTab);
			focusedTab.set(renderingTab);
		}

		Platform.runLater(() -> focusedTab.get().getContent().requestFocus());

		getDialogPane().setContent(tabPane);
	}

	private <T extends Node> T withAlignment(T node) {
		GridPane.setFillWidth(node, true);
		return node;
	}

	private Tab createTab(String name) {
		Tab tab = new Tab(name);
		tab.setClosable(false);
		return tab;
	}

	private GridPane createGrid() {
		GridPane grid = new GridPane();
		grid.getStyleClass().add("slider-grid-pane");
		return grid;
	}

	private void addPairToGrid(GridPane grid, int y, Label key, Node... value) {
		if (value.length == 0 || value.length > 2) {
			throw new IllegalArgumentException("invalid number of arguments (" + value.length + ") for addPairToGrid");
		}
		grid.add(key, 0, y, 1, 1);
		for (int i = 0; i < value.length; i++) {
			grid.add(withAlignment(value[i]), i + 1, y, value.length == 1 ? 2 : 1, 1);
		}
	}

	private Slider createSlider(int min, int max, int steps, int init) {
		if (max < min) {
			max = min;
		}
		Slider slider = new Slider(min, max, init);
		int majorTicks = Math.max((int) Math.ceil(max - min) / 5, 1);
		slider.setMajorTickUnit(majorTicks);
		slider.setMinorTickCount(majorTicks - 1);
		slider.setBlockIncrement(steps);
		return slider;
	}

	public static class Result {

		public final int readThreads, processThreads, writeThreads, maxLoadedFiles;
		public final Color regionColor, chunkColor, pasteColor;
		public final boolean shadeWater;
		public final boolean shade;
		public final boolean showNonexistentRegions;
		public final boolean smoothRendering, smoothOverlays;
		public final TileMapBox.TileMapBoxBackground tileMapBackground;
		public final File mcSavesDir;
		public final boolean debug;
		public final Locale locale;
		public final int height;
		public final boolean layerOnly, caves;
		public final File poi, entities;

		public Result(Locale locale, int readThreads, int processThreads, int writeThreads, int maxLoadedFiles,
		              Color regionColor, Color chunkColor, Color pasteColor, boolean shade, boolean shadeWater,
		              boolean showNonexistentRegions, boolean smoothRendering, boolean smoothOverlays,
		              TileMapBox.TileMapBoxBackground tileMapBackground, File mcSavesDir, boolean debug, int height,
		              boolean layerOnly, boolean caves, File poi, File entities) {

			this.locale = locale;
			this.readThreads = readThreads;
			this.processThreads = processThreads;
			this.writeThreads = writeThreads;
			this.maxLoadedFiles = maxLoadedFiles;
			this.regionColor = regionColor;
			this.chunkColor = chunkColor;
			this.pasteColor = pasteColor;
			this.shade = shade;
			this.shadeWater = shadeWater;
			this.showNonexistentRegions = showNonexistentRegions;
			this.smoothRendering = smoothRendering;
			this.smoothOverlays = smoothOverlays;
			this.tileMapBackground = tileMapBackground;
			this.mcSavesDir = Objects.requireNonNullElseGet(mcSavesDir, () -> new File(Config.DEFAULT_MC_SAVES_DIR));
			this.debug = debug;
			this.height = height;
			this.layerOnly = layerOnly;
			this.caves = caves;
			this.poi = poi;
			this.entities = entities;
		}
	}
}
