int main()
{
	int a[20][20],b[20][20],c[20][20];
	int i,j,k;
	
	for(i=0;i<20;i++)
	{
		for(j=0;j<20;j++)
		{
			for(k=0;k<20;k++)
			{
				c[i][k] += a[i][j]*b[j][k];
			}
		}
	}
	return 0;
}
