<?php
class Admin_Model_DbTable_Pictures extends Zend_Db_Table_Abstract
{
	protected $_name = 'pictures';
	protected $_primary = 'id';
	
	/**
     * Returns primary keys
     * @param	string	$thumb100
     * @param	string	$thumb170
     * @param	string	$thumb800
     * @return	int
     */
	public function setPicture($product_id, $thumb100, $thumb170, $thumb800)
	{
		$picsCount = $this->getCount($product_id);
		$data = array(
			'100x100'	=>	$thumb100,
			'170x170'	=>	$thumb170,
			'800x500'	=>	$thumb800,
			'active'	=>	($picsCount)?0:1,
			'order'		=>	($picsCount) + 1
		);
    	return $this->insert($data);    	
	}
	/**
     * Delete file
     * @param	string	$file
     * @return	void
     */
	public function delFile($file)
    {
    	unlink($file);    	
    }
	/**
     * Return array of pictures
     * @param int	$product_id
     * @return	array
     */
	public function getPictures($product_id)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('products2pictures',		  
		       		  'products2pictures.pictures_id = pictures.id', array())
			   ->where('products2pictures.products_id = ?', $product_id)
			   ->order('pictures.order ASC');
		$rows = $this->fetchAll($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}
	/**
     * Return COUNT od pictures
     * @param int	$product_id
     * @return	int
     */
	public function getCount($product_id)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('products2pictures',		  
		       		  'products2pictures.pictures_id = pictures.id', array())
			   ->where('products2pictures.products_id = ?', $product_id);
		$rows = $this->fetchAll($select);
		
		return count($rows);
	}
	/**
     * Change active
     * @param int	$product_id
     * @param int	$id
     * @return	void
     */
	public function changeActive($product_id, $id)
	{
		$pictures = $this->getPictures($product_id);
		$data = array('active'	=>	0);
		foreach ($pictures as $pic) {
			$this->update($data, 'id = '.$pic['id']); 
		}
		
		$select = $this->select();
		$select->where('id = ?', $id);
		$row = $this->fetchRow($select);
		
		if (count($row)==0) {
			return NULL;
		} else {
			$row = $row->toArray();
			$data = array(
				'active'	=>	($row['active'])?0:1
			);
    		$this->update($data, 'id = '.$id); 
			return $row;
		}
	}
	/**
     * Delete picture
     * @param int	$id
     * @return	void
     */
	public function delPicture($id)
    {
    	$product2PicturesTab = new Admin_Model_DbTable_Products2Pictures();
    	$product2PicturesTab->delProducts2Pictures($id);
    	
    	$select = $this->select();
		$select->where('id = ?', $id);
		$row = $this->fetchRow($select);
		
		$this->delFile($row['100x100']);
		$this->delFile($row['170x170']);
		$this->delFile($row['800x500']);
		
    	$this->delete('id = '.$id);
    }
	/**
     * Move right
     * @param int	$product_id
     * @param int	$id
     * @return	void
     */
	public function moveRight($product_id, $id)
    {
    	$pictures = $this->getPictures($product_id);
    	for ($i=0; $i<count($pictures); $i++) {
    		if ($pictures[$i]['id']==$id){
    			$order = $pictures[$i]['order'];
    			$right_id = $pictures[$i + 1]['id'];
    		}
    	}
    	
    	$data = array(
			'order'	=>	$order + 1
		);
    	$this->update($data, 'id = '.$id); 
    	
    	$data = array(
			'order'	=>	$order
		);
    	$this->update($data, 'id = '.$right_id); 
    }
	/**
     * Move left
     * @param int	$product_id
     * @param int	$id
     * @return	void
     */
	public function moveLeft($product_id, $id)
    {
    	$pictures = $this->getPictures($product_id);
    	for ($i=0; $i<count($pictures); $i++) {
    		if ($pictures[$i]['id']==$id){
    			$order = $pictures[$i]['order'];
    			$left_id = $pictures[$i - 1]['id'];
    		}
    	}
    	
    	$data = array(
			'order'	=>	$order - 1
		);
    	$this->update($data, 'id = '.$id); 
    	
    	$data = array(
			'order'	=>	$order
		);
    	$this->update($data, 'id = '.$left_id); 
    }
}