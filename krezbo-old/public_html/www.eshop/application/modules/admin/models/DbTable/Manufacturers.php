<?php
class Admin_Model_DbTable_Manufacturers extends Zend_Db_Table_Abstract
{
	protected $_name = 'manufacturers';
	protected $_primary = 'id';
	
	/**
     * Returns array of Manufacturers
     * @return	array
     */
	public function getManufacturers()
	{
		$select = $this->select();
		$select
			->order('title ASC')
			->where('id <> 1');
		$result = $this->fetchAll($select);
				
		if (count($result)==0) {
			return NULL;
		} else {
			$result = $result->toArray();
			return $result;
		}
	}
	/**
     * Return true/false if url exist
     * @param	string	$url
     * @return	boolean
     */
	public function existUrl($url)
	{
		$select = $this->select()->where('title_url = ?', $url);
		$result = $this->fetchAll($select);
		if($result)
		{
			return count($result);
		}
		return false;
	}
	/**
     * Returns primary keys
     * @param	string	$title
     * @param	string	$title_menu = ''
     * @param	string	$title_url = ''
     * @param	string	$description = ''
     * @return	int
     */
	public function setManufactury($title, $title_menu='', $title_url='', $description='')
	{
		$data = array('title'		=>	$title,
					  'title_menu'	=>	($title_menu=='')?$title:$title_menu,
    				  'title_url'	=>	($title_url=='')?$this->Convert($title):$this->Convert($title_url),
					  'description'	=>	$description);
    	return $this->insert($data);    	
	}
	/**
     * Returns array of Manufactury
     * @param	int	$id
     * @return	array
     */
	public function getManufactury($id)
	{
		$select = $this->select();
		$select->where('manufacturers.id = ?', $id);
		$result = $this->fetchRow($select);
				
		if (count($result)==0) {
			return NULL;
		} else {
			$result = $result->toArray();
			return $result;
		}
	}
	/**
     * Update manufactory
     * @param	int	$id
     * @param	string	$title
     * @param	string	$title_menu = ''
     * @param	string	$title_url = ''
     * @param	string	$description = ''
     * @return	void
     */
	public function updateManufactury($id, $title, $title_menu='', $title_url='', $description='')
	{
		$data = array('title'		=>	$title,
					  'title_menu'	=>	($title_menu=='')?$title:$title_menu,
    				  'title_url'	=>	($title_url=='')?$this->Convert($title):$this->Convert($title_url),
					  'description'	=>	$description);
		
		$this->update($data, 'id = '.$id);
	}
	/**
     * Delete manufactory
     * @param	int	$id
     * @return	void
     */
	public function delManufactory($id)
    {
    	/*$select = $this
    		->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
			->setIntegrityCheck(false);
    	$select
    		->from('products')
    		->where('manufacturers_id = ?', $id);
    	$result = $this->fetchAll($select);
    	if (count($result)==0) {
			
		} else {
			$result = $result->toArray();
    		foreach ($result as $row) {
    			
    		}
		}*/
    	//$productsTab = new Admin_Model_DbTable_Products();
    	//$productsTab->updateManufacturers($id, 0);
		$this->_db->update('products', array('manufacturers_id'=>1), 'manufacturers_id = '.$id);
    	$this->delete('id = '.$id);
    }
	/**
     * Return pairs
     * @return	array
     */
	public function getPairs()
	{
		$select = $this->select();
		$select
			//->where('flags IS NULL')
			->order('title ASC');
		$result = $this->_db->fetchPairs($select);
		
		if (count($result)==0) {
			return NULL;
		} else {
			//$rows = $rows->toArray();
			return $result;
		}
	}
	/**
     * Return converted text
     * @param	string	$text
     * @return	string
     */
	public function Convert($text)
	{
		$ar = array(' '=> '-', '&'=>'-', ':'=>'-', '.'=>'-', ','=>'-', '%'=>'-', '('=>'-', ')'=>'-', '+'=>'-', '/'=>'-',
					'á'=> 'a', 'č'=> 'c', 'ď'=> 'd', 'é'=> 'e', 'ě'=> 'e', 'í'=> 'i', 'ň'=> 'n', 'ó'=> 'o',
					'ř'=> 'r', 'š'=> 's', 'ť'=> 't', 'ú'=> 'u', 'ů'=> 'u', 'ý'=> 'y', 'ž'=> 'z',
					'Á'=> 'A', 'Č'=> 'C', 'Ď'=> 'D', 'É'=> 'E', 'Ě'=> 'E', 'Í'=> 'I', 'Ň'=> 'N', 'Ó'=> 'O',
					'Ř'=> 'R', 'Š'=> 'S', 'Ť'=> 'T', 'Ú'=> 'U', 'Ů'=> 'U', 'Ý'=> 'Y', 'Ž'=> 'Z' );
		
		foreach ($ar as $key=>$value) {
			$text = str_replace($key, $value, $text);
		}		
		$text = strtolower($text);
		return $text;
	}
	/**
     * set flag
	 * @param	int		$id
     * @param	string	$tag
     * @return	void
     */
	public function setFlag($id, $flag)
    {
    	$data = array('flags'	=>	$flag);
    	$this->update($data, 'id = '.$id);
    }
}