<?php
class Newsletter_Model_DbTable_Newsletters extends Zend_Db_Table_Abstract
{
	protected $_name = 'newsletters';
	protected $_primary = 'id';
	
	/**
     * Returns primary key
     * @param	string	$title
     * @param	string	$newsletter
     * @return	int
     */
	public function setNewsletter($title, $newsletter)
	{
		$data = array(
			'title'			=>	$title,
			'newsletter'	=>	$newsletter
		);
    	return $this->insert($data);    	
	}
	/**
     * Update newsletter
     * @param	int		$id
     * @param	string	$title
     * @param	string	$newsletter
     * @return	int
     */
	public function updateNewsletter($id, $title, $newsletter)
	{
		$data = array(
			'title'			=>	$title,
			'newsletter'	=>	$newsletter
		);
    	return $this->update($data, 'id = '.$id);    	
	}
	/**
     * Return array of newsletters
     * @param int	$page
     * @return	array
     */
	public function getNewsletters($page)
	{
		$select = $this->select();
		$select
			->where('flags IS NULL')
			->limitPage($page, 20);
		$rows = $this->fetchAll($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}
	/**
     * Return array of newsletter
     * @param int	$id
     * @return	array
     */
	public function getNewsletter($id)
	{
		$select = $this->select();
		$select->where('id = ?', $id);
		$row = $this->fetchRow($select);
		
		if (count($row)==0) {
			return NULL;
		} else {
			$row = $row->toArray();
			return $row;
		}
	}
	/**
     * Update sent
     * @param	int		$id
     * @param	boolean	$sent
     * @return	int
     */
	public function setSent($id, $sent)
	{
		$data = array(
			'sent'			=>	$sent
		);
    	return $this->update($data, 'id = '.$id);    	
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