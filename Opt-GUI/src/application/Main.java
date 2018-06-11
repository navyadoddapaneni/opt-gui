package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.examples.UpdateEmbeddedDoc;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;

public class Main extends Application {
	
	Label l;
	Group grp;
	File file1;
	XSSFSheet sheet;
	XSSFWorkbook workbook;
	ListView<MyObject> list;
	int rect_no = 0, rows = 0;
	double orgSceneX, orgSceneY;
	double orgTranslateX, orgTranslateY;
	ArrayList<Rectangle> module = new ArrayList<Rectangle>();
	HashMap<Integer, MyObject> module_label = new HashMap<Integer, MyObject>();

	@Override
	public void start(Stage primaryStage) {
		try {
			
			//buttons
			Button btn = new Button();
			btn.setText("  PDF chooser                          ");
			Button btn1 = new Button();
			btn1.setText("  Zoom In                                ");
			Button btn2 = new Button();
			btn2.setText("  Zoom Out                             ");
			Button btn3 = new Button();
			btn3.setText("  Rotate Image                        ");
			Button btn4 = new Button();
			btn4.setText("  Excel chooser                        ");
			Button btn5 = new Button();
			btn5.setText("  Save                                     ");
			
			//module shape
			Rectangle rectangle = new Rectangle(20, 20, 20, 20);
			rectangle.setFill(Color.GRAY);

			//list of module numbers
			list = new ListView<>();
			list.setPrefWidth(70); list.setPrefHeight(120);
			 
			//grid inside titledpane
			GridPane grid = new GridPane();
			grid.setVgap(4);
			grid.setPadding(new Insets(5, 5, 5, 5));
			grid.add(new Label("Mod No: "), 0, 0);
			grid.add(list, 1, 0);
			grid.add(new Label("Mod:    "), 0, 1);
			grid.add(rectangle, 1, 1);

			//titlepane for expand and collapse
			TitledPane titledPane = new TitledPane();
			titledPane.relocate(10, 70);
			titledPane.setAnimated(true);
			titledPane.setText("Test");
			titledPane.setMaxWidth(70.0);
			titledPane.setContent(grid);
			titledPane.setExpanded(true);

			Accordion accordion = new Accordion();
			accordion.getPanes().add(titledPane);

			//vertical box inside leftpane
			VBox vbox = new VBox();
			vbox.getChildren().add(btn);
			vbox.getChildren().add(btn4);
			vbox.getChildren().add(btn1);
			vbox.getChildren().add(btn2);
			vbox.getChildren().add(btn3);
			vbox.getChildren().add(btn5);
			vbox.getChildren().addAll(accordion);

			Pane leftPane = new Pane(new Label("Left"));
			leftPane.getChildren().add(vbox);

			grp = new Group();
			
			//scrollpane for rightside of splitpane
			ScrollPane scrollPane = new ScrollPane();
			scrollPane.setFitToHeight(true);
			scrollPane.setFitToWidth(true);
			scrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
			scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
			scrollPane.setContent(new Group(grp));

			SplitPane splitPane = new SplitPane();
			splitPane.getItems().addAll(leftPane, scrollPane);
			splitPane.setDividerPositions(0.25);
			leftPane.maxWidthProperty().bind(splitPane.widthProperty().multiply(0.17));

			Scene scene = new Scene(new BorderPane(splitPane), 800, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();

			//pdf chooser
			btn.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent event) {
					
					FileChooser fileChooser = new FileChooser();

					// Set Initial Directory to Desktop and set image in rightpane
					fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "\\Desktop"));
					File file = fileChooser.showOpenDialog(primaryStage);
					Image image = new Image(file.toURI().toString());
					ImageView imgView = new ImageView(image);
					imgView.setImage(image);
					imgView.setSmooth(true);
					grp.getChildren().add(imgView);
				}
			});
			
			//zoom in
			btn1.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent event) {
					
					grp.setScaleX(grp.getScaleX() * 1.5);
					grp.setScaleY(grp.getScaleY() * 1.5);
				}
			});

			//zoom out
			btn2.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent event) {

					grp.setScaleX(grp.getScaleX() / 1.5);
					grp.setScaleY(grp.getScaleY() / 1.5);
				}
			});

			//rotate
			btn3.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent event) {
					
					grp.setRotate(grp.getRotate() + 90);
				}
			});
			
			//excel chooser
			btn4.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent event) {
					
					FileChooser fileChooser = new FileChooser();
					// Set Initial Directory to Desktop and read the excel
					fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "\\Desktop"));
					file1 = fileChooser.showOpenDialog(primaryStage);
					if(read_excel(file1)==0){
					
						list.setCellFactory(new Callback<ListView<MyObject>, ListCell<MyObject>>(){
	
							@Override
							public ListCell<MyObject> call(ListView<MyObject> p) {
	
								ListCell<MyObject> cell = new ListCell<MyObject>(){
	
									@Override
									protected void updateItem(MyObject t, boolean bln) {
	
										super.updateItem(t, bln);
										if (t != null ) {
	
											setText( Integer.toString((int) Double.parseDouble(t.name)));		                           
										} 
										else {
											
											setText("");
										}
									}
								};
	
								return cell;	
							}
						});
					}else{
						update_list();
					}
				}
			});
			
			btn5.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent event) {
					
					write_toExcel(module_label);
				}
			});

			list.setOnMouseClicked(new EventHandler <MouseEvent>() {

				@Override 
				public void handle(MouseEvent event) {
					l = new Label();
					l.setText(Integer.toString((int) Double.parseDouble(list.getSelectionModel().getSelectedItem().name)));
				}

			});
		
			//do this when a drag is detected on the module on leftpane
			rectangle.setOnDragDetected(new EventHandler<MouseEvent>() {
				
				@Override
				public void handle(MouseEvent event) {
					
					Dragboard dragboard = rectangle.startDragAndDrop(TransferMode.COPY);
					ClipboardContent content = new ClipboardContent();
					content.putString("hello");
					dragboard.setContent(content);
					event.consume();
				}
			});
			
			//do this when module is being dragged onto rightpane
			grp.setOnDragOver(new EventHandler<DragEvent>() {
				
				@Override
				public void handle(DragEvent event) {

					event.acceptTransferModes(TransferMode.COPY);
					orgSceneX = event.getX();
					orgSceneY = event.getY();
					event.consume();
				}
			});
			
			//when the module is dropped set the location of module
			grp.setOnDragDropped(new EventHandler<DragEvent>() {
				
				@Override
				public void handle(DragEvent event) {

					Dragboard dragboard = event.getDragboard();
					if (dragboard.hasString()) {

						Rectangle rect = new Rectangle(20, 20, 20, 20);
						rect.setFill(Color.GREEN);
						rect.relocate(orgSceneX, orgSceneY);
						rect.setId("" + rect_no);
						module.add(rect);
						try {
							grp.getChildren().addAll(rect, l);
							l.relocate(orgSceneX, orgSceneY - 15);
							//System.out.println(list.getSelectionModel().getSelectedItem());
							module_label.put(rect_no, list.getSelectionModel().getSelectedItem());
							rect_no++;
							update_list();
							read_prop(rect ,list.getSelectionModel().getSelectedItem());

						} catch (Exception e) {
							Alert alert = new Alert(AlertType.ERROR);
							alert.setTitle("ERROR");
							alert.setContentText("The module is already created!");
							alert.showAndWait();
						}
						event.setDropCompleted(true);
					}
					event.consume();
				}

				
			});
			
			//drag is done
			grp.setOnDragDone(new EventHandler<DragEvent>() {

				@Override
				public void handle(DragEvent event) {

					event.consume();
				}
			});
			
			//read properties when mouse is clicked on module
			scrollPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
				
				int i = 0;
				@Override
				public void handle(MouseEvent event) {

					for (i = 0; i < module.size(); i++) {

						Rectangle m = module.get(i);
						module_clicked(m);
					}
				}
				private void module_clicked(Rectangle m) {

					m.setOnMouseClicked(new EventHandler<MouseEvent>() {

						@Override
						public void handle(MouseEvent event) {
							//System.out.println(module_label);
							//System.out.println(m.getId()+" "+module_label.get());
							pop_window(m, module_label.get(Integer.parseInt(m.getId())));
							
						}
					});
				}
			});

		}
	
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void update_list() {
		

		list.setCellFactory(new Callback<ListView<MyObject>, ListCell<MyObject>>(){

            @Override
            public ListCell<MyObject> call(ListView<MyObject> p) {

                ListCell<MyObject> cell = new ListCell<MyObject>(){

                    @Override
                    protected void updateItem(MyObject t, boolean bln) {
                		
                        super.updateItem(t, bln);
                        if (t != null ) {
                        	
                            setText( Integer.toString((int) Double.parseDouble(t.name)));
                            
                            if (l.getText().equals(Integer.toString((int) Double.parseDouble(t.name)))){
                               

                                if (!getStyleClass().contains("mystyleclass"))
                                {
                                    getStyleClass().add("mystyleclass");
                                }
                                t.color=1;

                            } 
                            else if(t.color==1)
                            {
                            	if (!getStyleClass().contains("mystyleclass")) 
                            	{
                                    getStyleClass().add("mystyleclass");
                                }
                            }
                            else {
                                getStyleClass().remove("mystyleclass");
                            }
                        } 
                        else {
                            setText("");
                        }
                    }

                };

                return cell;	
            }
		});
	}
   @SuppressWarnings("deprecation")
public void write_toExcel(HashMap<Integer, MyObject> module_label) {
	   
	   int count=0;
	   try {
       	
           FileInputStream file = new FileInputStream(file1);

           XSSFWorkbook workbook = new XSSFWorkbook(file);
           XSSFSheet sheet = workbook.getSheetAt(0);
          // XSSFCell cell = null;

           int rows = sheet.getPhysicalNumberOfRows();
		    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
           //Update the value of cell
           
           for(int r = 0; r <= rows && count < module_label.size(); r++) {
			      Row  row = sheet.getRow(r);
			        if(row != null) {
			        //    for(int c = 0; c < cols; c++) {
			        		XSSFCell cell =  (XSSFCell) row.getCell(2);
			                if(cell != null && !cell.toString().equals("Module No.")) {
			                	//System.out.println(cell.getCellTypeEnum()+" row "+ r );//int value = (int) cell.getNumericCellValue();
			                	CellValue cellvalue = evaluator.evaluate(cell);
			                	for(int i=0; i< module_label.size();i++)
			                	{
			                		//System.out.println(module_label.get(i).name);
			                		if(module_label.get(i).name.equals(Double.toString(cellvalue.getNumberValue()))){
			                		//	System.out.println("in");
			                			cell = (XSSFCell) row.getCell(0);
			                			writeallcells(cell,row,module_label.get(i).id,0);
			                			cell = (XSSFCell) row.getCell(1);
			                			writeallcells(cell,row,module_label.get(i).area,1);
			                			cell = (XSSFCell) row.getCell(3);
			                			writeallcells(cell,row,module_label.get(i).cat,3);
			                			cell = (XSSFCell) row.getCell(4);
			                			writeallcells(cell,row,module_label.get(i).des,4);
			                			cell = (XSSFCell) row.getCell(5);
			                			writeallcells(cell,row, module_label.get(i).width,5);
			                			cell = (XSSFCell) row.getCell(6);
			                			writeallcells(cell,row,module_label.get(i).length,6);
			                			cell = (XSSFCell) row.getCell(7);
			                			writeallcells(cell,row,module_label.get(i).height,7);
			                			cell = (XSSFCell) row.getCell(8);
			                			writeallcells(cell,row,module_label.get(i).ton,8);
			                			cell = (XSSFCell) row.getCell(9);
			                			writeallcells(cell,row,module_label.get(i).weight_mt,9);
			                			cell = (XSSFCell) row.getCell(10);
			                			writeallcells(cell,row,module_label.get(i).land,10);
			                			cell = (XSSFCell) row.getCell(11);
			                			writeallcells(cell,row,module_label.get(i).axe,11);
			                			cell = (XSSFCell) row.getCell(12);
			                			writeallcells(cell,row,module_label.get(i).lift,12);
			                			cell = (XSSFCell) row.getCell(13);
			                			writeallcells(cell,row,module_label.get(i).crane,13);
			                			cell = (XSSFCell) row.getCell(14);
			                			writeallcells(cell,row,module_label.get(i).ROS,14);
			                			cell = (XSSFCell) row.getCell(15);
			                			writeallcells(cell,row,module_label.get(i).set_date,15);
			                			cell = (XSSFCell) row.getCell(16);
			                			writeallcells(cell,row,module_label.get(i).remarks,16);
			                			cell = (XSSFCell) row.getCell(17);
			                			writeallcells(cell,row,module_label.get(i).x,17);
			                			cell = (XSSFCell) row.getCell(18);
			                			writeallcells(cell,row,module_label.get(i).y,18);
			              
			                			count++;
				                		break;
			                		}
			                		
				                					                		
			                	}
			                    
			                }
			       //     }
			        }
			   }
         

           file.close();

           FileOutputStream outFile =new FileOutputStream(file1);
           workbook.write(outFile);
           outFile.close();

       } catch (FileNotFoundException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }

		
	}
	@SuppressWarnings("deprecation")
	public void writeallcells(XSSFCell cell,Row row, String str, int cellno) {
		
		if(cell == null){
		    cell = (XSSFCell) row.createCell(cellno);
		}

	//	System.out.println(cell.getCellType());

		if (cell.getCellType() == XSSFCell.CELL_TYPE_BLANK) {
			
			cell.setCellValue(str);
			
		} else if (cell.getCellType() == XSSFCell.CELL_TYPE_ERROR) {
			
			cell.setCellValue(str);
			
		} else if (cell.getCellType() == XSSFCell.CELL_TYPE_FORMULA) {
		
			cell.setCellValue(str);
			
		} else if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
		
			cell.setCellValue(Double.parseDouble(str));
		} else if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
		
			cell.setCellValue(str);
		}
	}	
	// read excel and copy the module numbers to listview
	public int read_excel(File file12) {
		int flag=0;
		try {
			
			workbook = new XSSFWorkbook(file12);
			sheet = workbook.getSheetAt(0);
			rows = sheet.getPhysicalNumberOfRows();
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

			for (int r = 0; r <= rows; r++) {
				Row row = sheet.getRow(r);
				if (row != null) {
					// for(int c = 0; c < cols; c++) {
					Cell cell = row.getCell(2);
					
					if (cell != null && !cell.toString().equals("Module No.")) {
						
						CellValue cellvalue = evaluator.evaluate(cell);
						
						MyObject temp = new MyObject(Double.toString(cellvalue.getNumberValue()));
						
						cell = row.getCell(17);
						if(cell!=null){
							
							flag=1;
							
							 read_prop_excel(row,temp,null);
							 Rectangle rect = new Rectangle(20,20,20,20);
							 l = new Label(Integer.toString((int) Double.parseDouble(temp.name)));
							 rect.setId(Integer.toString(rect_no));
							 rect.setFill(Color.GREEN);
							 rect.relocate(Double.parseDouble(temp.x)+20, Double.parseDouble(temp.y)+20);
							 l.relocate(Double.parseDouble(temp.x)+20, Double.parseDouble(temp.y)+5);
							 grp.getChildren().addAll(rect, l);
							 module.add(rect);
							 module_label.put(rect_no, temp);
							 rect_no++;
							 temp.color=1;
							 
						}
						list.getItems().add(temp);
					}
					// }
				}
			}
			

		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return flag;
	}

	public static void main(String[] args) {
		launch(args);
	}

	// read props from excel and call pop window
	public void read_prop(Rectangle m, MyObject temp) {

		try {

			for (int r = 0; r < rows; r++) {
				Row row = sheet.getRow(r);
				if (row != null) {
					// for(int c = 0; c < cols; c++) {
					Cell cell = row.getCell(2);
					if (cell != null) {
						if (cell.toString().equals(temp.name)) {
							//System.out.println(temp.name);
							read_prop_excel(row, temp, m);
							break;
						}
					}
					// }
				}
			}
		} catch (Exception ioe) {
			
			ioe.printStackTrace();
		}
	}

    public void read_prop_excel(Row row, MyObject temp, Rectangle m) {
		
    	temp.id = row.getCell(0).toString();
		temp.area = row.getCell(1).toString();
		temp.name = row.getCell(2).toString();
		temp.cat = row.getCell(3).toString();
		temp.des = row.getCell(4).toString();
		temp.width = row.getCell(5).toString();
		temp.length = row.getCell(6).toString();
		temp.height = row.getCell(7).toString();
		temp.ton = row.getCell(8).toString();
		temp.weight_mt = row.getCell(9).toString();
		temp.land = row.getCell(10).toString();
		temp.axe = row.getCell(11).toString();
		temp.lift = row.getCell(12).toString();
		temp.crane = row.getCell(13).toString();
		temp.ROS = row.getCell(14).toString();
		temp.set_date = row.getCell(15).toString();
		temp.remarks = row.getCell(16).toString();
		if(m!=null){
			
			temp.x = Double.toString(m.getLayoutX());
			temp.y = Double.toString(m.getLayoutY());
		}
		else{
			
			temp.x = row.getCell(17).toString();
			temp.y = row.getCell(18).toString();
		}
		//System.out.println(temp.name);
	}
	//pop up new window to show the props of the module
	public void pop_window( Rectangle m, MyObject temp) {
		
		//System.out.println(temp);
		TextField id = new TextField(temp.id);
		TextField area = new TextField(temp.area);
		TextField mod_no = new TextField(temp.name);
		TextField cat = new TextField(temp.cat);
		TextField des = new TextField(temp.des);
		TextField width = new TextField(temp.width);
		TextField length = new TextField(temp.length);
		TextField height = new TextField(temp.height);
		TextField ton = new TextField(temp.ton);
		TextField weight_mt = new TextField(temp.weight_mt);
		TextField land = new TextField(temp.land);
		TextField axe = new TextField(temp.axe);
		TextField lift = new TextField(temp.lift);
		TextField crane = new TextField(temp.crane);
		TextField ROS = new TextField(temp.ROS);
		TextField set_date = new TextField(temp.set_date);
		TextField remarks = new TextField(temp.remarks);
		TextField x = new TextField(temp.x);
		TextField y = new TextField(temp.y);
 
		ScrollPane sp = new ScrollPane();
		sp.setFitToHeight(true);
		sp.setFitToWidth(true);
		sp.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		
		// insert values from excel to gridpane i.e, to the textfields
		GridPane prop = new GridPane();
		prop.setVgap(4);
		prop.setPadding(new Insets(5, 5, 5, 5));

		prop.add(new Label("Id No: "), 0, 0);
		prop.add(id, 1, 0);
		prop.add(new Label("Area: "), 0, 1);
		prop.add(area, 1, 1);
		prop.add(new Label("Module No: "), 0, 2);
		prop.add(mod_no, 1, 2);
		prop.add(new Label("Category: "), 0, 3);
		prop.add(cat, 1, 3);
		prop.add(new Label("Description: "), 0, 4);
		prop.add(des, 1, 4);
		prop.add(new Label("Width(m): "), 0, 5);
		prop.add(width, 1, 5);
		prop.add(new Label("Length(m): "), 0, 6);
		prop.add(length, 1, 6);
		prop.add(new Label("Height (m): "), 0, 7);
		prop.add(height, 1, 7);
		prop.add(new Label("Typical weight (Ton/m3): "), 0, 8);
		prop.add(ton, 1, 8);
		prop.add(new Label("Weight (mt): "), 0, 9);
		prop.add(weight_mt, 1, 9);
		prop.add(new Label("Land Transport: "), 0, 10);
		prop.add(land, 1, 10);
		prop.add(new Label("Axels: "), 0, 11);
		prop.add(axe, 1, 11);
		prop.add(new Label("Lifting Method: "), 0, 12);
		prop.add(lift, 1, 12);
		prop.add(new Label("Crane Lifting Capacity(mt): "), 0, 13);
		prop.add(crane, 1, 13);
		prop.add(new Label("Recevied on Site(ROS)(Mo): "), 0, 14);
		prop.add(ROS, 1, 14);
		prop.add(new Label("Setting Date(MO): "), 0, 15);
		prop.add(set_date, 1, 15);
		prop.add(new Label("Remarks: "), 0, 16);
		prop.add(remarks, 1, 16);
		prop.add(new Label("X-cord: "), 0, 17);
		prop.add(x , 1, 17);
		prop.add(new Label("Y-cord: "), 0, 18);
		prop.add(y , 1, 18);

		sp.setContent(prop);
		Stage stage = new Stage();
		stage.setTitle("Module Properties");
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		 
		//set Stage boundaries to the lower right corner of the visible bounds of the main screen
		stage.setX(primaryScreenBounds.getMinX() + primaryScreenBounds.getWidth() - 358);
		stage.setY(primaryScreenBounds.getMinY() + primaryScreenBounds.getHeight()- 680);
		stage.setScene(new Scene(new BorderPane(sp), 350, 450));
		stage.show();
		// Hide this current window (if this is what you want)
		// ((Node)(event.getSource())).getScene().getWindow().hide();
		
		
		id.textProperty().addListener((observable, oldValue, newValue) -> {
			
			for(int i=0;i<list.getItems().size();i++){
				
				if(mod_no.getText().equals(list.getItems().get(i).name)){
					
					list.getItems().get(i).id = newValue;
				}
			}
        	
        });
		area.textProperty().addListener((observable, oldValue, newValue) -> {
			
			for(int i=0;i<list.getItems().size();i++){
				
				if(mod_no.getText().equals(list.getItems().get(i).name)){
					
					list.getItems().get(i).area = newValue;
				}
			}
        	
        });
		cat.textProperty().addListener((observable, oldValue, newValue) -> {
			
			for(int i=0;i<list.getItems().size();i++){
				
				if(mod_no.getText().equals(list.getItems().get(i).name)){
					
					list.getItems().get(i).cat = newValue;
				}
			}
        	
        });	
		des.textProperty().addListener((observable, oldValue, newValue) -> {
			
			for(int i=0;i<list.getItems().size();i++){
				
				if(mod_no.getText().equals(list.getItems().get(i).name)){
					
					list.getItems().get(i).des = newValue;
				}
			}
        	
        });
		width.textProperty().addListener((observable, oldValue, newValue) -> {
			
			for(int i=0;i<list.getItems().size();i++){
				
				if(mod_no.getText().equals(list.getItems().get(i).name)){
					
					list.getItems().get(i).width = newValue;
				}
			}
        	
        });

		length.textProperty().addListener((observable, oldValue, newValue) -> {
			
			for(int i=0;i<list.getItems().size();i++){
				
				if(mod_no.getText().equals(list.getItems().get(i).name)){
					
					list.getItems().get(i).length = newValue;
				}
			}
        	
        });

		height.textProperty().addListener((observable, oldValue, newValue) -> {
			
			for(int i=0;i<list.getItems().size();i++){
				
				if(mod_no.getText().equals(list.getItems().get(i).name)){
					
					list.getItems().get(i).height = newValue;
				}
			}
		});
			
		ton.textProperty().addListener((observable, oldValue, newValue) -> {

			for(int i=0;i<list.getItems().size();i++){

				if(mod_no.getText().equals(list.getItems().get(i).name)){

					list.getItems().get(i).ton = newValue;
				}
			}

		});
		weight_mt.textProperty().addListener((observable, oldValue, newValue) -> {

			for(int i=0;i<list.getItems().size();i++){

				if(mod_no.getText().equals(list.getItems().get(i).name)){

					list.getItems().get(i).weight_mt = newValue;
				}
			}

		});
		land.textProperty().addListener((observable, oldValue, newValue) -> {

			for(int i=0;i<list.getItems().size();i++){

				if(mod_no.getText().equals(list.getItems().get(i).name)){

					list.getItems().get(i).land = newValue;
				}
			}

		});
		axe.textProperty().addListener((observable, oldValue, newValue) -> {

			for(int i=0;i<list.getItems().size();i++){

				if(mod_no.getText().equals(list.getItems().get(i).name)){

					list.getItems().get(i).axe = newValue;
				}
			}

		});
		lift.textProperty().addListener((observable, oldValue, newValue) -> {

			for(int i=0;i<list.getItems().size();i++){

				if(mod_no.getText().equals(list.getItems().get(i).name)){

					list.getItems().get(i).lift = newValue;
				}
			}

		});
		remarks.textProperty().addListener((observable, oldValue, newValue) -> {

			for(int i=0;i<list.getItems().size();i++){

				if(mod_no.getText().equals(list.getItems().get(i).name)){

					list.getItems().get(i).remarks = newValue;
				}
			}

		});
		crane.textProperty().addListener((observable, oldValue, newValue) -> {

			for(int i=0;i<list.getItems().size();i++){

				if(mod_no.getText().equals(list.getItems().get(i).name)){

					list.getItems().get(i).crane = newValue;
				}
			}

		});
		ROS.textProperty().addListener((observable, oldValue, newValue) -> {

			for(int i=0;i<list.getItems().size();i++){

				if(mod_no.getText().equals(list.getItems().get(i).name)){

					list.getItems().get(i).ROS = newValue;
				}
			}

		});
		set_date.textProperty().addListener((observable, oldValue, newValue) -> {

			for(int i=0;i<list.getItems().size();i++){

				if(mod_no.getText().equals(list.getItems().get(i).name)){

					list.getItems().get(i).set_date = newValue;
				}
			}

		});

	}
}
