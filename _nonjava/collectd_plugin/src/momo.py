import collectd

# Function to split DataType definition into string (used in map function)
def split_to_value(item):
	return item.split(":")[0]

# Read types.db to memory
def read_types_from_file():	
	global data_sources
	data_sources = {}
	
	types_file = open('./collectd_plugin/types.db', 'r')

	for str in types_file:
		exploded_line = str.split('\t',1)

		name = exploded_line[0]
		values_sequence = exploded_line[1]

		values = values_sequence.split(", ")
		values[0] = values[0].lstrip()

		data_sources[name] = map(split_to_value, values)
		
	types_file.close
	

def init(data=None):
	global f
	f = open('./collectd_plugin/file.txt', 'w') # open log file
	read_types_from_file() # read to mem
    

def shutdown(data=None):
	f.close
	
def write(vl, data=None):
	f.write(str(vl.time) + ";type:" + "org.collectd." + str(vl.plugin).capitalize() + ";type:" + str(vl.type_instance) + ";values:" + str(vl.values) + ";meta:" + str(data_sources[vl.type]) + "\n")
    
collectd.register_init(init);
collectd.register_shutdown(shutdown);
collectd.register_write(write);