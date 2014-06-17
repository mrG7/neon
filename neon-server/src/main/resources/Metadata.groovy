metadata {
	init {
		widget1 {
			initDataJson = '{"key1":"value1"}'
		}
	}
	columns {
		database1 {
			table1 {
				field1 {
					numeric = true
					logical = true
					temporal = true
					array = true
					object = true
					text = true
					heterogeneous = true
					nullable = true
				}
                field2 {
                    numeric = false
                    logical = false
                    temporal = false
                    array = false
                    object = false
                    text = false
                    heterogeneous = false
                    nullable = false
                }
			}
		}
	}
	widgets {
		database1 {
			table1 {
				widget1 {
					aSelector {
						value = "someValue"
					}
				}
			}
		}
        test {
            earthquakes {
                angular_example {
                    date {
                        value = "time"
                    }
                    latitude {
                        value = "latitude"
                    }
                    longitude {
                        value = "longitude"
                    }
                    line_x_axis {
                        value = "time"
                    }
                    bar_x_axis {
                        value = "net"
                    }
                    y_axis {
                        value = "mag"
                    }
                    color_by {
                        value = "magType"
                    }
                    size_by {
                        value = "mag"
                    }
                    sort_by {
                        value = "time"
                    }
                }
            }
            most_active {
                angular_example {
                    date {
                        value = "created_at"
                    }
                    latitude {
                        value = "latitude"
                    }
                    longitude {
                        value = "longitude"
                    }
                    line_x_axis {
                        value = "yyyy-mm-dd"
                    }
                    bar_x_axis {
                        value = "yyyy-mm"
                    }
                    y_axis {
                        value = "sentiment"
                    }
                    color_by {
                        value = "sentimentType"
                    }
                    size_by {
                        value = "sentiment"
                    }
                    sort_by {
                        value = "created_at"
                    }
                }
            }
            gbDate {
                angular_example {
                    date {
                        value = "created_at"
                    }
                    latitude {
                        value = "latitude"
                    }
                    longitude {
                        value = "longitude"
                    }
                    line_x_axis {
                        value = "yyyy-mm-dd"
                    }
                    bar_x_axis {
                        value = "yyyy-mm"
                    }
                    y_axis {
                        value = "sentiment"
                    }
                    color_by {
                        value = "sentimentType"
                    }
                    size_by {
                        value = "sentiment"
                    }
                    sort_by {
                        value = "created_at"
                    }
                }
            }
        }
	}
}
